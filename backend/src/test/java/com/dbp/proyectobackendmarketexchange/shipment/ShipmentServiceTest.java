package com.dbp.proyectobackendmarketexchange.shipment;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.InvalidShipmentTransitionException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.notification.domain.NotificationService;
import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentStatus;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentAddressUpdateDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private TradeProposalRepository tradeProposalRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private Clock clock = Clock.systemDefaultZone();

    @InjectMocks
    private ShipmentService shipmentService;

    private Usuario proposer;
    private Usuario receiver;
    private TradeProposal tradeProposal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        proposer = new Usuario();
        proposer.setId(1L);
        proposer.setFirstname("Proposer");
        proposer.setLastname("LastName");
        proposer.setEmail("proposer@example.com");
        proposer.setPhone("123456789");
        proposer.setPassword("password123");
        proposer.setAddress("Proposer Address");
        proposer.setRole(Role.USER);
        proposer.setCreatedAt(LocalDateTime.now());

        receiver = new Usuario();
        receiver.setId(2L);
        receiver.setFirstname("Receiver");
        receiver.setLastname("ReceiverLastName");
        receiver.setEmail("receiver@example.com");
        receiver.setPhone("987654321");
        receiver.setPassword("password321");
        receiver.setAddress("Receiver Address");
        receiver.setRole(Role.USER);
        receiver.setCreatedAt(LocalDateTime.now());

        tradeProposal = new TradeProposal();
        tradeProposal.setId(100L);
        tradeProposal.setProposer(proposer);
        tradeProposal.setReceiver(receiver);
        tradeProposal.setStatus(TradeStatus.ACCEPTED);

        Item offeredItem = new Item();
        offeredItem.setId(11L);
        offeredItem.setStatus(ItemStatus.RESERVED);
        Item requestedItem = new Item();
        requestedItem.setId(12L);
        requestedItem.setStatus(ItemStatus.RESERVED);
        tradeProposal.setOfferedItem(offeredItem);
        tradeProposal.setRequestedItem(requestedItem);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, java.util.Collections.emptyList()));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    @Test
    void testGetAllShipments() {
        Shipment shipment1 = new Shipment();
        Shipment shipment2 = new Shipment();

        when(shipmentRepository.findAll()).thenReturn(Arrays.asList(shipment1, shipment2));

        List<ShipmentResponseDto> shipments = shipmentService.getAllShipments();

        assertNotNull(shipments);
        assertEquals(2, shipments.size());
        verify(shipmentRepository, times(1)).findAll();
    }

    @Test
    void testCreateShipmentForTradeProposal_Accepted() {
        when(shipmentRepository.existsByTradeProposalId(100L)).thenReturn(false);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shipmentService.createShipmentForTradeProposal(tradeProposal);

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void testCreateShipmentForTradeProposal_NotAccepted() {
        tradeProposal.setStatus(TradeStatus.PENDING);

        assertThrows(IllegalStateException.class, () -> shipmentService.createShipmentForTradeProposal(tradeProposal));
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void testCreateShipmentForTradeProposal_AlreadyExists_NoOp() {
        when(shipmentRepository.existsByTradeProposalId(100L)).thenReturn(true);

        shipmentService.createShipmentForTradeProposal(tradeProposal);

        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    private Shipment buildShipment(ShipmentStatus status) {
        Shipment shipment = new Shipment();
        shipment.setId(1L);
        shipment.setTradeProposal(tradeProposal);
        shipment.setInitiatorAddress("Proposer Address");
        shipment.setReceiveAddress("Receiver Address");
        shipment.setStatus(status);
        return shipment;
    }

    @Test
    void testGetShipmentById_ParticipantAllowed() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);

        ShipmentResponseDto result = shipmentService.getShipmentById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getTradeProposalId());
    }

    @Test
    void testGetShipmentById_NotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> shipmentService.getShipmentById(1L));
    }

    @Test
    void testGetShipmentById_Forbidden() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> shipmentService.getShipmentById(1L));
    }

    @Test
    void testUpdateAddresses_ProposerEditsInitiatorAddress() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentAddressUpdateDto dto = new ShipmentAddressUpdateDto();
        dto.setInitiatorAddress("Nueva direccion proposer");

        ShipmentResponseDto result = shipmentService.updateAddresses(1L, dto);

        assertEquals("Nueva direccion proposer", result.getInitiatorAddress());
        verify(authorizationUtils, never()).isAdminOrResourceOwner(2L);
    }

    @Test
    void testUpdateAddresses_ProposerCannotEditReceiveAddress() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);
        when(authorizationUtils.isAdminOrResourceOwner(2L)).thenReturn(false);

        ShipmentAddressUpdateDto dto = new ShipmentAddressUpdateDto();
        dto.setReceiveAddress("Direccion que no le pertenece");

        assertThrows(ForbiddenOperationException.class, () -> shipmentService.updateAddresses(1L, dto));
    }

    @Test
    void testUpdateAddresses_BlockedAfterShipped() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);

        ShipmentAddressUpdateDto dto = new ShipmentAddressUpdateDto();
        dto.setInitiatorAddress("Otra direccion");

        assertThrows(IllegalStateException.class, () -> shipmentService.updateAddresses(1L, dto));
    }

    @Test
    void testPrepareShipment_Success() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponseDto result = shipmentService.prepareShipment(1L);

        assertEquals(ShipmentStatus.PREPARING, result.getStatus());
        assertNotNull(result.getPreparedAt());
    }

    @Test
    void testPrepareShipment_InvalidTransition() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);

        assertThrows(InvalidShipmentTransitionException.class, () -> shipmentService.prepareShipment(1L));
    }

    @Test
    void testPrepareShipment_ForbiddenForReceiver() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> shipmentService.prepareShipment(1L));
    }

    @Test
    void testShipShipment_Success() {
        Shipment shipment = buildShipment(ShipmentStatus.PREPARING);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponseDto result = shipmentService.shipShipment(1L, "TRACK-123");

        assertEquals(ShipmentStatus.IN_TRANSIT, result.getStatus());
        assertEquals("TRACK-123", result.getTrackingCode());
        assertNotNull(result.getShippedAt());
    }

    @Test
    void testShipShipment_DuplicateTrackingCode_TranslatesToInvalidTransition() {
        Shipment shipment = buildShipment(ShipmentStatus.PREPARING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(shipmentRepository.save(shipment))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key"));

        assertThrows(InvalidShipmentTransitionException.class, () -> shipmentService.shipShipment(1L, "TRACK-DUP"));
    }

    @Test
    void testShipShipment_InvalidTransition() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);

        assertThrows(InvalidShipmentTransitionException.class, () -> shipmentService.shipShipment(1L, null));
    }

    @Test
    void testDeliverShipment_Success_CompletesTradeProposal() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);
        shipment.setProposerDeliveryConfirmedAt(LocalDateTime.now());
        authenticateAs(receiver);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);
        when(tradeProposalRepository.save(tradeProposal)).thenReturn(tradeProposal);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentResponseDto result = shipmentService.deliverShipment(1L);

        assertEquals(ShipmentStatus.DELIVERED, result.getStatus());
        assertNotNull(result.getDeliveredAt());
        assertEquals(TradeStatus.COMPLETED, tradeProposal.getStatus());
        assertEquals(ItemStatus.EXCHANGED, tradeProposal.getOfferedItem().getStatus());
        assertEquals(ItemStatus.EXCHANGED, tradeProposal.getRequestedItem().getStatus());
    }

    @Test
    void testDeliverShipment_ForbiddenForProposer() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> shipmentService.deliverShipment(1L));
    }

    @Test
    void testDeliverShipment_CannotDeliverTwice() {
        Shipment shipment = buildShipment(ShipmentStatus.DELIVERED);
        authenticateAs(receiver);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponseDto result = shipmentService.deliverShipment(1L);

        assertEquals(ShipmentStatus.DELIVERED, result.getStatus());
    }

    @Test
    void testCancelShipment_AllowedFromPending() {
        Shipment shipment = buildShipment(ShipmentStatus.PENDING);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponseDto result = shipmentService.cancelShipment(1L);

        assertEquals(ShipmentStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getCancelledAt());
    }

    @Test
    void testCancelShipment_AllowedFromPreparing() {
        Shipment shipment = buildShipment(ShipmentStatus.PREPARING);
        authenticateAs(proposer);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentResponseDto result = shipmentService.cancelShipment(1L);

        assertEquals(ShipmentStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelShipment_ProhibitedFromInTransit() {
        Shipment shipment = buildShipment(ShipmentStatus.IN_TRANSIT);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);

        assertThrows(InvalidShipmentTransitionException.class, () -> shipmentService.cancelShipment(1L));
    }

    @Test
    void testCancelShipment_ProhibitedFromDelivered() {
        Shipment shipment = buildShipment(ShipmentStatus.DELIVERED);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(authorizationUtils.isAdminOrResourceOwner(1L, 2L)).thenReturn(true);

        assertThrows(InvalidShipmentTransitionException.class, () -> shipmentService.cancelShipment(1L));
    }

    @Test
    void testDeleteShipment() {
        doNothing().when(shipmentRepository).deleteById(1L);

        shipmentService.deleteShipment(1L);

        verify(shipmentRepository, times(1)).deleteById(1L);
    }
}
