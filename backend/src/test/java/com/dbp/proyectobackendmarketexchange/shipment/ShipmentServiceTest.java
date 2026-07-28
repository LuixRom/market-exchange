package com.dbp.proyectobackendmarketexchange.shipment;

import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentRequestDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

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
    private ModelMapper modelMapper;

    @InjectMocks
    private ShipmentService shipmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllShipments() {
        Shipment shipment1 = new Shipment();
        Shipment shipment2 = new Shipment();
        ShipmentResponseDto shipmentDto1 = new ShipmentResponseDto();
        ShipmentResponseDto shipmentDto2 = new ShipmentResponseDto();

        when(shipmentRepository.findAll()).thenReturn(Arrays.asList(shipment1, shipment2));
        when(modelMapper.map(shipment1, ShipmentResponseDto.class)).thenReturn(shipmentDto1);
        when(modelMapper.map(shipment2, ShipmentResponseDto.class)).thenReturn(shipmentDto2);

        List<ShipmentResponseDto> shipments = shipmentService.getAllShipments();

        assertNotNull(shipments);
        assertEquals(2, shipments.size());
        verify(shipmentRepository, times(1)).findAll();
    }

    @Test
    public void testCreateShipmentForTradeProposal_Accepted() {
        Usuario proposer = new Usuario();
        proposer.setFirstname("Proposer");
        proposer.setLastname("LastName");
        proposer.setEmail("proposer@example.com");
        proposer.setPhone("123456789");
        proposer.setPassword("password123");
        proposer.setAddress("Proposer Address");
        proposer.setRole(Role.USER);
        proposer.setCreatedAt(LocalDateTime.now());

        Usuario receiver = new Usuario();
        receiver.setFirstname("Receiver");
        receiver.setLastname("ReceiverLastName");
        receiver.setEmail("receiver@example.com");
        receiver.setPhone("987654321");
        receiver.setPassword("password321");
        receiver.setAddress("Receiver Address");
        receiver.setRole(Role.USER);
        receiver.setCreatedAt(LocalDateTime.now());

        TradeProposal tradeProposal = new TradeProposal();
        tradeProposal.setStatus(TradeStatus.ACCEPTED);
        tradeProposal.setProposer(proposer);
        tradeProposal.setReceiver(receiver);

        Shipment shipment = new Shipment();
        shipment.setInitiatorAddress(proposer.getAddress());
        shipment.setReceiveAddress(receiver.getAddress());
        shipment.setDeliveryDate(LocalDateTime.now().plusDays(7));

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        shipmentService.createShipmentForTradeProposal(tradeProposal);

        verify(shipmentRepository, times(1)).save(any(Shipment.class));

        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(shipmentCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertEquals("Proposer Address", savedShipment.getInitiatorAddress());
        assertEquals("Receiver Address", savedShipment.getReceiveAddress());
        assertNotNull(savedShipment.getDeliveryDate());
        assertEquals(tradeProposal, savedShipment.getTradeProposal());
    }

    @Test
    public void testCreateShipmentForTradeProposal_NotAccepted() {
        TradeProposal tradeProposal = new TradeProposal();
        tradeProposal.setStatus(TradeStatus.PENDING);

        assertThrows(IllegalStateException.class, () -> shipmentService.createShipmentForTradeProposal(tradeProposal));
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    public void testGetShipmentById_Success() {
        Shipment shipment = new Shipment();
        shipment.setId(1L);
        ShipmentResponseDto shipmentResponseDto = new ShipmentResponseDto();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(modelMapper.map(shipment, ShipmentResponseDto.class)).thenReturn(shipmentResponseDto);

        ShipmentResponseDto result = shipmentService.getShipmentById(1L);

        assertNotNull(result);
        verify(shipmentRepository, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(shipment, ShipmentResponseDto.class);
    }

    @Test
    public void testGetShipmentById_NotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shipmentService.getShipmentById(1L));
        verify(shipmentRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateShipment() {
        Shipment existingShipment = new Shipment();
        existingShipment.setId(1L);
        existingShipment.setInitiatorAddress("Old Initiator Address");
        existingShipment.setReceiveAddress("Old Recipient Address");
        existingShipment.setDeliveryDate(LocalDateTime.now());

        ShipmentRequestDto shipmentRequestDto = new ShipmentRequestDto();
        shipmentRequestDto.setInitiatorAddress("New Initiator Address");
        shipmentRequestDto.setReceiveAddress("New Recipient Address");
        shipmentRequestDto.setDeliveryDate(LocalDateTime.now().plusDays(7));
        shipmentRequestDto.setTradeProposalId(1L);

        TradeProposal tradeProposal = new TradeProposal();
        tradeProposal.setId(1L);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(existingShipment));
        when(tradeProposalRepository.findById(1L)).thenReturn(Optional.of(tradeProposal));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(existingShipment);

        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setInitiatorAddress("New Initiator Address");
        responseDto.setReceiveAddress("New Recipient Address");

        when(modelMapper.map(any(Shipment.class), eq(ShipmentResponseDto.class))).thenReturn(responseDto);

        ShipmentResponseDto result = shipmentService.updateShipment(1L, shipmentRequestDto);

        assertNotNull(result);
        assertEquals("New Initiator Address", result.getInitiatorAddress());
        assertEquals("New Recipient Address", result.getReceiveAddress());

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    public void testUpdateShipment_NotFound() {
        when(shipmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        ShipmentRequestDto shipmentRequestDto = new ShipmentRequestDto();
        assertThrows(ResourceNotFoundException.class, () -> shipmentService.updateShipment(1L, shipmentRequestDto));
        verify(shipmentRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testDeleteShipment() {
        doNothing().when(shipmentRepository).deleteById(anyLong());

        shipmentService.deleteShipment(1L);

        verify(shipmentRepository, times(1)).deleteById(1L);
    }
}
