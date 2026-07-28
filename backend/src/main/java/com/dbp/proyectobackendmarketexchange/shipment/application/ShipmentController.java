package com.dbp.proyectobackendmarketexchange.shipment.application;

import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentAddressUpdateDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentShipRequestDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    // Solo ADMIN (regla catch-all /shipments/** en SecurityConfig) — listado global.
    @GetMapping
    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    @GetMapping("/{id}")
    public ShipmentResponseDto getShipmentById(@PathVariable Long id) {
        return shipmentService.getShipmentById(id);
    }

    // Editar direcciones únicamente — ya no acepta ni aplica "status".
    @PutMapping("/{id}")
    public ShipmentResponseDto updateAddresses(@PathVariable Long id, @Valid @RequestBody ShipmentAddressUpdateDto dto) {
        return shipmentService.updateAddresses(id, dto);
    }

    @PutMapping("/{id}/prepare")
    public ShipmentResponseDto prepareShipment(@PathVariable Long id) {
        return shipmentService.prepareShipment(id);
    }

    @PutMapping("/{id}/ship")
    public ShipmentResponseDto shipShipment(@PathVariable Long id, @Valid @RequestBody(required = false) ShipmentShipRequestDto dto) {
        String trackingCode = dto != null ? dto.getTrackingCode() : null;
        return shipmentService.shipShipment(id, trackingCode);
    }

    @PutMapping("/{id}/deliver")
    public ShipmentResponseDto deliverShipment(@PathVariable Long id) {
        return shipmentService.deliverShipment(id);
    }

    @PutMapping("/{id}/cancel")
    public ShipmentResponseDto cancelShipment(@PathVariable Long id) {
        return shipmentService.cancelShipment(id);
    }

    // Eliminar un envío por ID (ADMIN, vía la regla catch-all)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}
