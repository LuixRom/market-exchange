package com.dbp.proyectobackendmarketexchange.shipment.domain;

import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentRequestDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;

    public ShipmentService(ShipmentRepository shipmentRepository, TradeProposalRepository tradeProposalRepository, ItemRepository itemRepository, ModelMapper modelMapper) {
        this.shipmentRepository = shipmentRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.itemRepository = itemRepository;
        this.modelMapper = modelMapper;
    }

    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(shipment -> modelMapper.map(shipment, ShipmentResponseDto.class))
                .collect(Collectors.toList());
    }

    public void createShipmentForTradeProposal(TradeProposal tradeProposal) {
        if (tradeProposal.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("La propuesta debe estar en estado ACCEPTED para crear un envío");
        }

        if (shipmentRepository.existsByTradeProposalId(tradeProposal.getId())) {
            return;
        }

        Shipment shipment = new Shipment();
        shipment.setInitiatorAddress(tradeProposal.getProposer().getAddress());
        shipment.setReceiveAddress(tradeProposal.getReceiver().getAddress());

        // Asignar una fecha de entrega predeterminada (7 días después)
        shipment.setDeliveryDate(LocalDateTime.now().plusDays(7));

        shipment.setTradeProposal(tradeProposal);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipmentRepository.save(shipment);
    }

    public ShipmentResponseDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        return modelMapper.map(shipment, ShipmentResponseDto.class);
    }

    @Transactional
    public ShipmentResponseDto updateShipment(Long id, ShipmentRequestDto shipmentRequestDto) {
        Shipment existingShipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (existingShipment.getStatus() == ShipmentStatus.DELIVERED || existingShipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("No se puede modificar un envío que ya está " + existingShipment.getStatus());
        }

        boolean justDelivered = shipmentRequestDto.getStatus() == ShipmentStatus.DELIVERED
                && existingShipment.getStatus() != ShipmentStatus.DELIVERED;

        mapRequestDtoToShipment(shipmentRequestDto, existingShipment);
        Shipment updatedShipment = shipmentRepository.save(existingShipment);

        if (justDelivered) {
            completeTradeProposal(updatedShipment.getTradeProposal());
        }

        return modelMapper.map(updatedShipment, ShipmentResponseDto.class);
    }

    private void completeTradeProposal(TradeProposal tradeProposal) {
        if (tradeProposal.getStatus() != TradeStatus.ACCEPTED) {
            return;
        }

        tradeProposal.setStatus(TradeStatus.COMPLETED);
        tradeProposalRepository.save(tradeProposal);

        Item offeredItem = tradeProposal.getOfferedItem();
        Item requestedItem = tradeProposal.getRequestedItem();
        offeredItem.setStatus(ItemStatus.EXCHANGED);
        requestedItem.setStatus(ItemStatus.EXCHANGED);
        itemRepository.save(offeredItem);
        itemRepository.save(requestedItem);
    }

    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
    }

    private void mapRequestDtoToShipment(ShipmentRequestDto dto, Shipment shipment) {
        shipment.setInitiatorAddress(dto.getInitiatorAddress());
        shipment.setReceiveAddress(dto.getReceiveAddress());
        shipment.setDeliveryDate(dto.getDeliveryDate());
        if (dto.getStatus() != null) {
            shipment.setStatus(dto.getStatus());
        }

        TradeProposal tradeProposal = tradeProposalRepository.findById(dto.getTradeProposalId())
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        shipment.setTradeProposal(tradeProposal);
    }
}
