package com.dbp.proyectobackendmarketexchange.rating.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.exception.DuplicateRatingException;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.RatingNotAllowedException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingReputationDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import com.dbp.proyectobackendmarketexchange.rating.infrastructure.RatingRepository;
import com.dbp.proyectobackendmarketexchange.notification.domain.NotificationService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingService {
    private static final Duration RATING_CREATION_WINDOW = Duration.ofDays(14);
    private static final Duration RATING_EDIT_WINDOW = Duration.ofHours(48);

    private final RatingRepository ratingRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthorizationUtils authorizationUtils;
    private final NotificationService notificationService;
    private final Clock clock;

    public RatingService(RatingRepository ratingRepository, TradeProposalRepository tradeProposalRepository,
                          UsuarioRepository usuarioRepository,
                          AuthorizationUtils authorizationUtils,
                          NotificationService notificationService,
                          Clock clock) {
        this.ratingRepository = ratingRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.usuarioRepository = usuarioRepository;
        this.authorizationUtils = authorizationUtils;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    public RatingResponseDto crearRating(RatingRequestDto requestDTO) {
        Usuario reviewer = resolveCurrentUser();

        TradeProposal tradeProposal = tradeProposalRepository.findById(requestDTO.getTradeProposalId())
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));

        if (tradeProposal.getStatus() != TradeStatus.COMPLETED) {
            throw new RatingNotAllowedException("Solo se puede calificar una propuesta de intercambio en estado COMPLETED");
        }
        assertRatingCreationWindowOpen(tradeProposal);

        // El reviewedUser se deriva server-side como "la contraparte del trade" — nunca
        // viene del cliente. Sin bypass de ADMIN acá a propósito: un administrador no
        // debería poder hacerse pasar por una de las partes para emitir una calificación.
        Usuario reviewedUser;
        if (tradeProposal.getProposer().getId().equals(reviewer.getId())) {
            reviewedUser = tradeProposal.getReceiver();
        } else if (tradeProposal.getReceiver().getId().equals(reviewer.getId())) {
            reviewedUser = tradeProposal.getProposer();
        } else {
            throw new ForbiddenOperationException("Solo el proponente o el receptor de este intercambio pueden calificarlo");
        }

        if (ratingRepository.existsByTradeProposalIdAndReviewerId(tradeProposal.getId(), reviewer.getId())) {
            throw new DuplicateRatingException("Ya calificaste esta propuesta de intercambio");
        }

        Rating rating = new Rating();
        rating.setTradeProposal(tradeProposal);
        rating.setReviewer(reviewer);
        rating.setReviewedUser(reviewedUser);
        rating.setScore(requestDTO.getScore());
        rating.setCommunicationScore(requestDTO.getCommunicationScore());
        rating.setPunctualityScore(requestDTO.getPunctualityScore());
        rating.setItemConditionScore(requestDTO.getItemConditionScore());
        rating.setComment(requestDTO.getComment());

        try {
            rating = ratingRepository.save(rating);
        } catch (DataIntegrityViolationException e) {
            // Backstop de base de datos ante una carrera (dos requests casi simultáneos
            // del mismo reviewer para la misma propuesta) — mismo patrón que
            // TradeProposalService.createTradeProposal.
            throw new DuplicateRatingException("Ya calificaste esta propuesta de intercambio");
        }

        notificationService.create(reviewedUser, "RATING_RECEIVED", "Nueva calificacion",
                "Recibiste una nueva calificacion por un intercambio completado", tradeProposal.getId(), null);

        return convertirAResponseDTO(rating);
    }

    public RatingResponseDto actualizarRating(Long ratingId, RatingRequestDto requestDTO) {
        Usuario reviewer = resolveCurrentUser();
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
        if (!rating.getReviewer().getId().equals(reviewer.getId())) {
            throw new ForbiddenOperationException("Solo quien emitio el rating puede editarlo");
        }
        if (rating.getCreatedAt().plus(RATING_EDIT_WINDOW).isBefore(LocalDateTime.now(clock))) {
            throw new RatingNotAllowedException("La ventana de edicion del rating ya expiro");
        }

        rating.setScore(requestDTO.getScore());
        rating.setCommunicationScore(requestDTO.getCommunicationScore());
        rating.setPunctualityScore(requestDTO.getPunctualityScore());
        rating.setItemConditionScore(requestDTO.getItemConditionScore());
        rating.setComment(requestDTO.getComment());
        return convertirAResponseDTO(ratingRepository.save(rating));
    }

    public List<RatingResponseDto> listarRatings() {
        return ratingRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<RatingResponseDto> obtenerRatingsPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return ratingRepository.findByReviewedUserId(usuarioId).stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public RatingReputationDto getReputation(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        RatingReputationDto dto = new RatingReputationDto();
        dto.setUserId(usuarioId);
        dto.setAverageScore(ratingRepository.findAverageScoreByReviewedUserId(usuarioId).orElse(null));
        dto.setRatingCount(ratingRepository.countByReviewedUserId(usuarioId));
        return dto;
    }

    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));

        // Solo ADMIN: ni el reviewer ni el reviewedUser pueden borrar una calificación ya
        // emitida (antes el reviewedUser podía borrar una calificación desfavorable sobre
        // sí mismo — bug de integridad corregido en esta fase).
        if (!authorizationUtils.isAdmin()) {
            throw new ForbiddenOperationException("Solo un administrador puede eliminar una calificación");
        }

        ratingRepository.delete(rating);
    }

    private Usuario resolveCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private void assertRatingCreationWindowOpen(TradeProposal tradeProposal) {
        LocalDateTime completedAt = tradeProposal.getUpdatedAt() != null
                ? tradeProposal.getUpdatedAt()
                : tradeProposal.getCreatedAt();
        if (completedAt != null && completedAt.plus(RATING_CREATION_WINDOW).isBefore(LocalDateTime.now(clock))) {
            throw new RatingNotAllowedException("La ventana para calificar este intercambio ya expiro");
        }
    }

    private RatingResponseDto convertirAResponseDTO(Rating rating) {
        RatingResponseDto responseDTO = new RatingResponseDto();
        responseDTO.setId(rating.getId());
        responseDTO.setTradeProposalId(rating.getTradeProposal().getId());
        responseDTO.setScore(rating.getScore());
        responseDTO.setCommunicationScore(rating.getCommunicationScore());
        responseDTO.setPunctualityScore(rating.getPunctualityScore());
        responseDTO.setItemConditionScore(rating.getItemConditionScore());
        responseDTO.setComment(rating.getComment());
        responseDTO.setReviewerId(rating.getReviewer().getId());
        responseDTO.setReviewerName(rating.getReviewer().getFirstname() + " " + rating.getReviewer().getLastname());
        responseDTO.setReviewedUserId(rating.getReviewedUser().getId());
        responseDTO.setReviewedUserName(rating.getReviewedUser().getFirstname() + " " + rating.getReviewedUser().getLastname());
        responseDTO.setCreatedAt(rating.getCreatedAt());
        responseDTO.setUpdatedAt(rating.getUpdatedAt());
        return responseDTO;
    }
}
