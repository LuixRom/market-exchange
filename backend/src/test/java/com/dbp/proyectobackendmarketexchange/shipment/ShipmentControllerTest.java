package com.dbp.proyectobackendmarketexchange.shipment;

import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.shipment.application.ShipmentController;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentStatus;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentAddressUpdateDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentShipRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentService shipmentService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetAllShipments() throws Exception {
        when(shipmentService.getAllShipments()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(shipmentService, times(1)).getAllShipments();
    }

    @Test
    void testGetShipmentById() throws Exception {
        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);

        when(shipmentService.getShipmentById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/shipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(shipmentService, times(1)).getShipmentById(1L);
    }

    @Test
    void testUpdateAddresses() throws Exception {
        ShipmentAddressUpdateDto requestDto = new ShipmentAddressUpdateDto();
        requestDto.setInitiatorAddress("Nueva direccion");

        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setInitiatorAddress("Nueva direccion");

        when(shipmentService.updateAddresses(eq(1L), any(ShipmentAddressUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/shipments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initiatorAddress").value("Nueva direccion"));

        verify(shipmentService, times(1)).updateAddresses(eq(1L), any(ShipmentAddressUpdateDto.class));
    }

    @Test
    void testPrepareShipment() throws Exception {
        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(ShipmentStatus.PREPARING);

        when(shipmentService.prepareShipment(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/shipments/1/prepare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));

        verify(shipmentService, times(1)).prepareShipment(1L);
    }

    @Test
    void testShipShipment() throws Exception {
        ShipmentShipRequestDto requestDto = new ShipmentShipRequestDto();
        requestDto.setTrackingCode("TRACK-1");

        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(ShipmentStatus.IN_TRANSIT);
        responseDto.setTrackingCode("TRACK-1");

        when(shipmentService.shipShipment(1L, "TRACK-1")).thenReturn(responseDto);

        mockMvc.perform(put("/shipments/1/ship")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.trackingCode").value("TRACK-1"));

        verify(shipmentService, times(1)).shipShipment(1L, "TRACK-1");
    }

    @Test
    void testShipShipment_NoBody() throws Exception {
        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(ShipmentStatus.IN_TRANSIT);

        when(shipmentService.shipShipment(1L, null)).thenReturn(responseDto);

        mockMvc.perform(put("/shipments/1/ship"))
                .andExpect(status().isOk());

        verify(shipmentService, times(1)).shipShipment(1L, null);
    }

    @Test
    void testDeliverShipment() throws Exception {
        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(ShipmentStatus.DELIVERED);

        when(shipmentService.deliverShipment(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/shipments/1/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(shipmentService, times(1)).deliverShipment(1L);
    }

    @Test
    void testCancelShipment() throws Exception {
        ShipmentResponseDto responseDto = new ShipmentResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(ShipmentStatus.CANCELLED);

        when(shipmentService.cancelShipment(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/shipments/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(shipmentService, times(1)).cancelShipment(1L);
    }

    @Test
    void testDeleteShipment() throws Exception {
        doNothing().when(shipmentService).deleteShipment(1L);

        mockMvc.perform(delete("/shipments/1"))
                .andExpect(status().isNoContent());

        verify(shipmentService, times(1)).deleteShipment(1L);
    }
}
