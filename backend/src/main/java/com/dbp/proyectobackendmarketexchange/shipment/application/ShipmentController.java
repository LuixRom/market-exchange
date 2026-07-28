package com.dbp.proyectobackendmarketexchange.shipment.application;

import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentRequestDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final TradeProposalRepository tradeProposalRepository;

    public ShipmentController(ShipmentService shipmentService, TradeProposalRepository tradeProposalRepository) {
        this.shipmentService = shipmentService;
        this.tradeProposalRepository = tradeProposalRepository;
    }

    // Obtener todos los envíos
    @GetMapping
    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    // Crear un nuevo envío
    @PostMapping
    public ShipmentResponseDto createShipmentForTradeProposal(@Valid @RequestBody ShipmentRequestDto shipmentRequestDto) {
        TradeProposal tradeProposal = tradeProposalRepository.findById(shipmentRequestDto.getTradeProposalId())
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));

        shipmentService.createShipmentForTradeProposal(tradeProposal);

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
