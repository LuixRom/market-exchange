package com.dbp.proyectobackendmarketexchange.shipment.application;

import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import com.dbp.proyectobackendmarketexchange.agreement.infrastructure.AgreementRepository;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentRequestDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final AgreementRepository agreementRepository;

    public ShipmentController(ShipmentService shipmentService, AgreementRepository agreementRepository) {
        this.shipmentService = shipmentService;
        this.agreementRepository = agreementRepository;
    }

    // Obtener todos los envíos
    @GetMapping
    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    // Crear un nuevo envío
    @PostMapping
    public ShipmentResponseDto createShipmentForAgreement(@Valid @RequestBody ShipmentRequestDto shipmentRequestDto) {
        Agreement agreement = agreementRepository.findById(shipmentRequestDto.getAgreementId())
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        shipmentService.createShipmentForAgreement(agreement);

        return new ShipmentResponseDto();
    }

    // Obtener un envío por ID
    @GetMapping("/{id}")
    public ShipmentResponseDto getShipmentById(@PathVariable Long id) {
        return shipmentService.getShipmentById(id);
    }

    // Actualizar un envío por ID
    @PutMapping("/{id}")
    public ShipmentResponseDto updateShipment(@PathVariable Long id, @Valid @RequestBody ShipmentRequestDto shipmentRequestDto) {
        return shipmentService.updateShipment(id, shipmentRequestDto);
    }

    // Eliminar un envío por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<ShipmentResponseDto> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}