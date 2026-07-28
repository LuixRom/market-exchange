package com.dbp.proyectobackendmarketexchange.tradeproposal;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.event.tradeproposal.TradeProposalAcceptedEvent;
import com.dbp.proyectobackendmarketexchange.event.tradeproposal.TradeProposalCreatedEvent;
import com.dbp.proyectobackendmarketexchange.event.tradeproposal.TradeProposalRejectedEvent;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.InvalidTradeProposalException;
import com.dbp.proyectobackendmarketexchange.exception.TradeProposalConflictException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposalService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalRequestDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalSummary;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TradeProposalServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private TradeProposalRepository tradeProposalRepository;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @InjectMocks
    private TradeProposalService tradeProposalService;

    private Usuario proposer;
    private Usuario receiver;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        proposer = new Usuario();
        proposer.setId(1L);
        proposer.setEmail("proposer@example.com");

        receiver = new Usuario();
        receiver.setId(2L);
        receiver.setEmail("receiver@example.com");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList()));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    private Item buildItem(Long id, Usuario owner, ItemStatus status) {
        Item item = new Item();
        item.setId(id);
        item.setName("Item " + id);
        item.setUsuario(owner);
        item.setStatus(status);
        return item;
    }

    // ---- createTradeProposal ----

    @Test
    public void testCreateTradeProposal_Success() {
        authenticateAs(proposer);

        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(requestedItem));
        when(tradeProposalRepository.existsByOfferedItemIdAndRequestedItemIdAndStatus(10L, 20L, TradeStatus.PENDING))
                .thenReturn(false);
        when(tradeProposalRepository.save(any(TradeProposal.class))).thenAnswer(invocation -> {
            TradeProposal saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        TradeProposalResponseDto result = tradeProposalService.createTradeProposal(requestDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(TradeStatus.PENDING, result.getStatus());
        assertEquals(10L, result.getOfferedItemId());
        assertEquals(20L, result.getRequestedItemId());
        assertEquals(1L, result.getProposerId());
        assertEquals(2L, result.getReceiverId());
        verify(eventPublisher, times(1)).publishEvent(any(TradeProposalCreatedEvent.class));
    }

    @Test
    public void testCreateTradeProposal_NotOwnerOfOfferedItem() {
        authenticateAs(proposer);

        Item offeredItem = buildItem(10L, receiver, ItemStatus.APPROVED); // no es del proposer
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(requestedItem));

        assertThrows(InvalidTradeProposalException.class, () -> tradeProposalService.createTradeProposal(requestDto));
        verify(tradeProposalRepository, never()).save(any());
    }

    @Test
    public void testCreateTradeProposal_OfferedItemNotApproved() {
        authenticateAs(proposer);

        Item offeredItem = buildItem(10L, proposer, ItemStatus.PENDING_REVIEW);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(requestedItem));

        assertThrows(InvalidTradeProposalException.class, () -> tradeProposalService.createTradeProposal(requestDto));
    }

    @Test
    public void testCreateTradeProposal_RequestedItemNotApproved() {
        authenticateAs(proposer);

        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.RESERVED);

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(requestedItem));

        assertThrows(InvalidTradeProposalException.class, () -> tradeProposalService.createTradeProposal(requestDto));
    }

    @Test
    public void testCreateTradeProposal_SameUser() {
        authenticateAs(proposer);

        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, proposer, ItemStatus.APPROVED); // mismo dueño

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(requestedItem));

        assertThrows(InvalidTradeProposalException.class, () -> tradeProposalService.createTradeProposal(requestDto));
    }

    @Test
    public void testCreateTradeProposal_SameItem() {
        authenticateAs(proposer);

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(10L);

        assertThrows(InvalidTradeProposalException.class, () -> tradeProposalService.createTradeProposal(requestDto));
        verify(itemRepository, never()).findById(any());
    }

    @Test
    public void testCreateTradeProposal_DuplicateActiveProposal() {
        authenticateAs(proposer);

        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);

        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findById(20L)).thenReturn(Optional.of(requestedItem));
        when(tradeProposalRepository.existsByOfferedItemIdAndRequestedItemIdAndStatus(10L, 20L, TradeStatus.PENDING))
                .thenReturn(true);

        assertThrows(TradeProposalConflictException.class, () -> tradeProposalService.createTradeProposal(requestDto));
        verify(tradeProposalRepository, never()).save(any());
    }

    // ---- acceptTradeProposal ----

    private TradeProposal buildPendingProposal(Item offeredItem, Item requestedItem) {
        TradeProposal proposal = new TradeProposal();
        proposal.setId(100L);
        proposal.setOfferedItem(offeredItem);
        proposal.setRequestedItem(requestedItem);
        proposal.setProposer(proposer);
        proposal.setReceiver(receiver);
        proposal.setStatus(TradeStatus.PENDING);
        return proposal;
    }

    private TradeProposalSummary buildSummary(Item offeredItem, Item requestedItem, Long receiverId) {
        TradeProposalSummary summary = mock(TradeProposalSummary.class);
        when(summary.getOfferedItemId()).thenReturn(offeredItem.getId());
        when(summary.getRequestedItemId()).thenReturn(requestedItem.getId());
        when(summary.getReceiverId()).thenReturn(receiverId);
        return summary;
    }

    @Test
    public void testAcceptTradeProposal_Success() {
        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);
        TradeProposal proposal = buildPendingProposal(offeredItem, requestedItem);

        TradeProposalSummary summary = buildSummary(offeredItem, requestedItem, 2L);
        when(tradeProposalRepository.findSummaryById(100L)).thenReturn(Optional.of(summary));
        when(authorizationUtils.isAdminOrResourceOwner(2L)).thenReturn(true);
        when(itemRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(requestedItem));
        when(tradeProposalRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(proposal));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeProposalRepository.save(any(TradeProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tradeProposalRepository.cancelCompetingProposals(anyList(), eq(100L))).thenReturn(1);

        TradeProposalResponseDto result = tradeProposalService.acceptTradeProposal(100L);

        assertNotNull(result);
        assertEquals(TradeStatus.ACCEPTED, result.getStatus());
        assertEquals(ItemStatus.RESERVED, offeredItem.getStatus());
        assertEquals(ItemStatus.RESERVED, requestedItem.getStatus());
        verify(tradeProposalRepository, times(1)).cancelCompetingProposals(anyList(), eq(100L));
        verify(shipmentService, times(1)).createShipmentForTradeProposal(proposal);
        verify(eventPublisher, times(1)).publishEvent(any(TradeProposalAcceptedEvent.class));
    }

    @Test
    public void testAcceptTradeProposal_ForbiddenWhenNotReceiverNorAdmin() {
        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);
        TradeProposal proposal = buildPendingProposal(offeredItem, requestedItem);

        TradeProposalSummary summary = buildSummary(offeredItem, requestedItem, 2L);
        when(tradeProposalRepository.findSummaryById(100L)).thenReturn(Optional.of(summary));
        when(authorizationUtils.isAdminOrResourceOwner(2L)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> tradeProposalService.acceptTradeProposal(100L));
        verify(itemRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    public void testAcceptTradeProposal_ConflictWhenAlreadyCancelledByCompetitor() {
        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);
        TradeProposal proposal = buildPendingProposal(offeredItem, requestedItem);

        TradeProposal alreadyCancelled = buildPendingProposal(offeredItem, requestedItem);
        alreadyCancelled.setStatus(TradeStatus.CANCELLED);

        TradeProposalSummary summary = buildSummary(offeredItem, requestedItem, 2L);
        when(tradeProposalRepository.findSummaryById(100L)).thenReturn(Optional.of(summary));
        when(authorizationUtils.isAdminOrResourceOwner(2L)).thenReturn(true);
        when(itemRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(offeredItem));
        when(itemRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(requestedItem));
        when(tradeProposalRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(alreadyCancelled));

        assertThrows(TradeProposalConflictException.class, () -> tradeProposalService.acceptTradeProposal(100L));
        verify(shipmentService, never()).createShipmentForTradeProposal(any());
    }

    // ---- rejectTradeProposal ----

    @Test
    public void testRejectTradeProposal_Success() {
        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);
        TradeProposal proposal = buildPendingProposal(offeredItem, requestedItem);

        TradeProposalSummary summary = buildSummary(offeredItem, requestedItem, 2L);
        when(tradeProposalRepository.findSummaryById(100L)).thenReturn(Optional.of(summary));
        when(authorizationUtils.isAdminOrResourceOwner(2L)).thenReturn(true);
        when(tradeProposalRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(proposal));
        when(tradeProposalRepository.save(any(TradeProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TradeProposalResponseDto result = tradeProposalService.rejectTradeProposal(100L);

        assertEquals(TradeStatus.REJECTED, result.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(TradeProposalRejectedEvent.class));
    }

    @Test
    public void testRejectTradeProposal_ForbiddenWhenNotReceiverNorAdmin() {
        Item offeredItem = buildItem(10L, proposer, ItemStatus.APPROVED);
        Item requestedItem = buildItem(20L, receiver, ItemStatus.APPROVED);
        TradeProposal proposal = buildPendingProposal(offeredItem, requestedItem);

        TradeProposalSummary summary = buildSummary(offeredItem, requestedItem, 2L);
        when(tradeProposalRepository.findSummaryById(100L)).thenReturn(Optional.of(summary));
        when(authorizationUtils.isAdminOrResourceOwner(2L)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> tradeProposalService.rejectTradeProposal(100L));
        verify(tradeProposalRepository, never()).save(any());
    }
}
