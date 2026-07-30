package com.dbp.proyectobackendmarketexchange.raiting;


import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.exception.DuplicateRatingException;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.RatingNotAllowedException;
import com.dbp.proyectobackendmarketexchange.notification.domain.NotificationService;
import com.dbp.proyectobackendmarketexchange.rating.domain.Rating;
import com.dbp.proyectobackendmarketexchange.rating.domain.RatingService;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingReputationDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import com.dbp.proyectobackendmarketexchange.rating.infrastructure.RatingRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private TradeProposalRepository tradeProposalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RatingService ratingService;

    private Usuario proposer;
    private Usuario receiver;
    private TradeProposal completedTrade;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        proposer = new Usuario();
        proposer.setId(1L);
        proposer.setEmail("proposer@example.com");
        proposer.setFirstname("Luis");
        proposer.setLastname("Perez");

        receiver = new Usuario();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");
        receiver.setFirstname("Juan");
        receiver.setLastname("Lopez");

        completedTrade = new TradeProposal();
        completedTrade.setId(50L);
        completedTrade.setProposer(proposer);
        completedTrade.setReceiver(receiver);
        completedTrade.setStatus(TradeStatus.COMPLETED);
        completedTrade.setUpdatedAt(java.time.LocalDateTime.now());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, java.util.Collections.emptyList()));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    @Test
    public void testCrearRating_ProposerRatesReceiver() {
        authenticateAs(proposer);

        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setTradeProposalId(50L);
        requestDto.setScore(4);
        requestDto.setComment("Buen servicio");

        when(tradeProposalRepository.findById(50L)).thenReturn(Optional.of(completedTrade));
        when(ratingRepository.existsByTradeProposalIdAndReviewerId(50L, 1L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(java.time.LocalDateTime.now());
            return saved;
        });

        RatingResponseDto result = ratingService.crearRating(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getReviewerId());
        assertEquals(2L, result.getReviewedUserId());
        assertEquals(4, result.getScore());
    }

    @Test
    public void testCrearRating_ReceiverRatesProposer() {
        authenticateAs(receiver);

        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setTradeProposalId(50L);
        requestDto.setScore(5);
        requestDto.setComment("Todo bien");

        when(tradeProposalRepository.findById(50L)).thenReturn(Optional.of(completedTrade));
        when(ratingRepository.existsByTradeProposalIdAndReviewerId(50L, 2L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RatingResponseDto result = ratingService.crearRating(requestDto);

        assertNotNull(result);
        assertEquals(2L, result.getReviewerId());
        assertEquals(1L, result.getReviewedUserId());
    }

    @Test
    public void testCrearRating_RejectedWhenNotCompleted() {
        authenticateAs(proposer);
        completedTrade.setStatus(TradeStatus.ACCEPTED);

        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setTradeProposalId(50L);
        requestDto.setScore(4);

        when(tradeProposalRepository.findById(50L)).thenReturn(Optional.of(completedTrade));

        assertThrows(RatingNotAllowedException.class, () -> ratingService.crearRating(requestDto));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    public void testCrearRating_ReviewerOutsideTrade_Forbidden() {
        Usuario outsider = new Usuario();
        outsider.setId(99L);
        outsider.setEmail("outsider@example.com");
        authenticateAs(outsider);

        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setTradeProposalId(50L);
        requestDto.setScore(3);

        when(tradeProposalRepository.findById(50L)).thenReturn(Optional.of(completedTrade));

        assertThrows(ForbiddenOperationException.class, () -> ratingService.crearRating(requestDto));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    public void testCrearRating_DuplicateRejected() {
        authenticateAs(proposer);

        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setTradeProposalId(50L);
        requestDto.setScore(4);

        when(tradeProposalRepository.findById(50L)).thenReturn(Optional.of(completedTrade));
        when(ratingRepository.existsByTradeProposalIdAndReviewerId(50L, 1L)).thenReturn(true);

        assertThrows(DuplicateRatingException.class, () -> ratingService.crearRating(requestDto));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    public void testListarRatings() {
        Rating rating1 = new Rating();
        rating1.setId(1L);
        rating1.setScore(5);
        rating1.setReviewer(proposer);
        rating1.setReviewedUser(receiver);
        rating1.setTradeProposal(completedTrade);

        when(ratingRepository.findAll()).thenReturn(List.of(rating1));

        List<RatingResponseDto> result = ratingService.listarRatings();

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getScore());
        verify(ratingRepository, times(1)).findAll();
    }

    @Test
    public void testObtenerRatingsPorUsuario() {
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(ratingRepository.findByReviewedUserId(2L)).thenReturn(List.of());

        ratingService.obtenerRatingsPorUsuario(2L);

        verify(ratingRepository, times(1)).findByReviewedUserId(2L);
    }

    @Test
    public void testGetReputation() {
        when(usuarioRepository.existsById(2L)).thenReturn(true);
        when(ratingRepository.findAverageScoreByReviewedUserId(2L)).thenReturn(Optional.of(4.5));
        when(ratingRepository.countByReviewedUserId(2L)).thenReturn(2L);

        RatingReputationDto result = ratingService.getReputation(2L);

        assertEquals(2L, result.getUserId());
        assertEquals(4.5, result.getAverageScore());
        assertEquals(2L, result.getRatingCount());
    }

    @Test
    public void testDeleteRating_AdminAllowed() {
        Rating rating = new Rating();
        rating.setId(1L);
        rating.setReviewedUser(receiver);

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));
        when(authorizationUtils.isAdmin()).thenReturn(true);

        ratingService.deleteRating(1L);

        verify(ratingRepository, times(1)).delete(rating);
    }

    @Test
    public void testDeleteRating_NonAdminForbidden() {
        Rating rating = new Rating();
        rating.setId(1L);
        rating.setReviewedUser(receiver);

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));
        when(authorizationUtils.isAdmin()).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> ratingService.deleteRating(1L));
        verify(ratingRepository, never()).delete(any());
    }
}
