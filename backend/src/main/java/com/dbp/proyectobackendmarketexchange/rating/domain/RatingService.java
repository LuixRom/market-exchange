package com.dbp.proyectobackendmarketexchange.rating.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.exception.DuplicateRatingException;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.RatingNotAllowedException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingReputationDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import com.dbp.proyectobackendmarketexchange.rating.infrastructure.RatingRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthorizationUtils authorizationUtils;

    public RatingService(RatingRepository ratingRepository, TradeProposalRepository tradeProposalRepository,
                          UsuarioRepository usuarioRepository, AuthorizationUtils authorizationUtils) {
        this.ratingRepository = ratingRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.usuarioRepository = usuarioRepository;
        this.authorizationUtils = authorizationUtils;
    }

    public RatingResponseDto crearRating(RatingRequestDto requestDTO) {
        Usuario reviewer = resolveCurrentUser();

        TradeProposal tradeProposal = tradeProposalRepository.findById(requestDTO.getTradeProposalId())
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));

        if (tradeProposal.getStatus() != TradeStatus.COMPLETED) {
            throw new RatingNotAllowedException("Solo se puede calificar una propuesta de intercambio en estado COMPLETED");
        }

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
        rating.setComment(requestDTO.getComment());

        try {
            rating = ratingRepository.save(rating);
        } catch (DataIntegrityViolationException e) {
            // Backstop de base de datos ante una carrera (dos requests casi simultáneos
            // del mismo reviewer para la misma propuesta) — mismo patrón que
            // TradeProposalService.createTradeProposal.
            throw new DuplicateRatingException("Ya calificaste esta propuesta de intercambio");
        }

        return convertirAResponseDTO(rating);
    }

    public List<RatingResponseDto> listarRatings() {
        return ratingRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<RatingResponseDto> obtenerRatingsPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return ratingRepository.findByReviewedUserId(usuarioId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
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
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private RatingResponseDto convertirAResponseDTO(Rating rating) {
        RatingResponseDto responseDTO = new RatingResponseDto();
        responseDTO.setId(rating.getId());
        responseDTO.setTradeProposalId(rating.getTradeProposal().getId());
        responseDTO.setScore(rating.getScore());
        responseDTO.setComment(rating.getComment());
        responseDTO.setReviewerId(rating.getReviewer().getId());
        responseDTO.setReviewerName(rating.getReviewer().getFirstname() + " " + rating.getReviewer().getLastname());
        responseDTO.setReviewedUserId(rating.getReviewedUser().getId());
        responseDTO.setReviewedUserName(rating.getReviewedUser().getFirstname() + " " + rating.getReviewedUser().getLastname());
        responseDTO.setCreatedAt(rating.getCreatedAt());
        return responseDTO;
    }
}
