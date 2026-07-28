package com.dbp.proyectobackendmarketexchange.shipment.domain;

import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import com.dbp.proyectobackendmarketexchange.agreement.infrastructure.AgreementRepository;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentRequestDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;


import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AgreementRepository agreementRepository;
    private final ModelMapper modelMapper;

    public ShipmentService(ShipmentRepository shipmentRepository, AgreementRepository agreementRepository, ModelMapper modelMapper) {
        this.shipmentRepository = shipmentRepository;
        this.agreementRepository = agreementRepository;
        this.modelMapper = modelMapper;
    }

    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(shipment -> modelMapper.map(shipment, ShipmentResponseDto.class))
                .collect(Collectors.toList());
    }

    public void createShipmentForAgreement(Agreement agreement) {
        // Validar el estado del acuerdo
        if (agreement.getState() != State.ACCEPTED) {
            throw new IllegalStateException("El acuerdo debe estar en estado ACCEPTED para crear un envío");
        }

        Shipment shipment = new Shipment();
        shipment.setInitiatorAddress(agreement.getInitiator().getAddress());
        shipment.setReceiveAddress(agreement.getRecipient().getAddress());

        // Asignar una fecha de entrega predeterminada (7 días después)
        shipment.setDeliveryDate(LocalDateTime.now().plusDays(7));

        shipment.setAgreement(agreement);
        shipmentRepository.save(shipment);
    }

    public ShipmentResponseDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        return modelMapper.map(shipment, ShipmentResponseDto.class);
    }

    public ShipmentResponseDto updateShipment(Long id, ShipmentRequestDto shipmentRequestDto) {
        Shipment existingShipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        mapRequestDtoToShipment(shipmentRequestDto, existingShipment);
        Shipment updatedShipment = shipmentRepository.save(existingShipment);
        return modelMapper.map(updatedShipment, ShipmentResponseDto.class);
    }

    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }

    private void mapRequestDtoToShipment(ShipmentRequestDto dto, Shipment shipment) {
        shipment.setInitiatorAddress(dto.getInitiatorAddress());
        shipment.setReceiveAddress(dto.getReceiveAddress());
        shipment.setDeliveryDate(dto.getDeliveryDate());

        Agreement agreement = agreementRepository.findById(dto.getAgreementId())
                .orElseThrow(() -> new RuntimeException("Agreement not found"));
        shipment.setAgreement(agreement);
    }
}