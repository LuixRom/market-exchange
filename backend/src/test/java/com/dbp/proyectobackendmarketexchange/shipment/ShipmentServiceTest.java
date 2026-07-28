package com.dbp.proyectobackendmarketexchange.shipment;

import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import com.dbp.proyectobackendmarketexchange.agreement.infrastructure.AgreementRepository;
import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentRequestDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
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
    private AgreementRepository agreementRepository;

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
    public void testCreateShipmentForAgreement_Accepted() {
        // Crear usuarios completos
        Usuario initiator = new Usuario();
        initiator.setFirstname("Initiator");
        initiator.setLastname("InitiatorLastName");
        initiator.setEmail("initiator@example.com");
        initiator.setPhone("123456789");
        initiator.setPassword("password123");
        initiator.setAddress("Initiator Address");
        initiator.setRole(Role.USER);
        initiator.setCreatedAt(LocalDateTime.now());

        Usuario recipient = new Usuario();
        recipient.setFirstname("Recipient");
        recipient.setLastname("RecipientLastName");
        recipient.setEmail("recipient@example.com");
        recipient.setPhone("987654321");
        recipient.setPassword("password321");
        recipient.setAddress("Recipient Address");
        recipient.setRole(Role.USER);
        recipient.setCreatedAt(LocalDateTime.now());

        // Crear el acuerdo con el estado ACCEPTED
        Agreement agreement = new Agreement();
        agreement.setState(State.ACCEPTED);
        agreement.setInitiator(initiator);
        agreement.setRecipient(recipient);

        // Crear un envío
        Shipment shipment = new Shipment();
        shipment.setInitiatorAddress(initiator.getAddress());
        shipment.setReceiveAddress(recipient.getAddress());
        shipment.setDeliveryDate(LocalDateTime.now().plusDays(7));

        // Mockear la función save del repositorio
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        // Ejecutar el servicio para crear el envío
        shipmentService.createShipmentForAgreement(agreement);

        // Verificar que se haya guardado el envío
        verify(shipmentRepository, times(1)).save(any(Shipment.class));

        // Verificar el contenido del envío guardado
        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(shipmentCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertEquals("Initiator Address", savedShipment.getInitiatorAddress());
        assertEquals("Recipient Address", savedShipment.getReceiveAddress());
        assertNotNull(savedShipment.getDeliveryDate());
        assertEquals(agreement, savedShipment.getAgreement());
    }


    @Test
    public void testCreateShipmentForAgreement_NotAccepted() {
        Agreement agreement = new Agreement();
        agreement.setState(State.PENDING); // Estado no aceptado

        assertThrows(IllegalStateException.class, () -> shipmentService.createShipmentForAgreement(agreement));
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

        assertThrows(RuntimeException.class, () -> shipmentService.getShipmentById(1L));
        verify(shipmentRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateShipment() {
        // Preparar los datos de prueba
        Shipment existingShipment = new Shipment();
        existingShipment.setId(1L);
        existingShipment.setInitiatorAddress("Old Initiator Address");
        existingShipment.setReceiveAddress("Old Recipient Address");
        existingShipment.setDeliveryDate(LocalDateTime.now());

        ShipmentRequestDto shipmentRequestDto = new ShipmentRequestDto();
        shipmentRequestDto.setInitiatorAddress("New Initiator Address");
        shipmentRequestDto.setReceiveAddress("New Recipient Address");
        shipmentRequestDto.setDeliveryDate(LocalDateTime.now().plusDays(7));
        shipmentRequestDto.setAgreementId(1L);

        Agreement agreement = new Agreement();
        agreement.setId(1L);

        // Simular el comportamiento de los repositorios y el mapeador
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(existingShipment));
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(agreement));

        // Asegúrate de que save() retorne el existingShipment actualizado
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(existingShipment);

        // Simular la conversión del ModelMapper
        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setInitiatorAddress("New Initiator Address");
        responseDto.setReceiveAddress("New Recipient Address");

        when(modelMapper.map(any(Shipment.class), eq(ShipmentResponseDto.class))).thenReturn(responseDto);

        // Ejecutar el metodo que se va a probar
        ShipmentResponseDto result = shipmentService.updateShipment(1L, shipmentRequestDto);

        // Verificar que el resultado no sea nulo
        assertNotNull(result);
        assertEquals("New Initiator Address", result.getInitiatorAddress());
        assertEquals("New Recipient Address", result.getReceiveAddress());

        // Verificar que se llamaron los métodos correctos en los repositorios
        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }



    @Test
    public void testUpdateShipment_NotFound() {
        when(shipmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        ShipmentRequestDto shipmentRequestDto = new ShipmentRequestDto();
        assertThrows(RuntimeException.class, () -> shipmentService.updateShipment(1L, shipmentRequestDto));
        verify(shipmentRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testDeleteShipment() {
        doNothing().when(shipmentRepository).deleteById(anyLong());

        shipmentService.deleteShipment(1L);

        verify(shipmentRepository, times(1)).deleteById(1L);
    }
}
