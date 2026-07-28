package com.dbp.proyectobackendmarketexchange.shipment.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.InvalidShipmentTransitionException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentAddressUpdateDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;

import org.springframework.dao.DataIntegrityViolationException;
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
    private final AuthorizationUtils authorizationUtils;

    public ShipmentService(ShipmentRepository shipmentRepository, TradeProposalRepository tradeProposalRepository,
                            ItemRepository itemRepository, AuthorizationUtils authorizationUtils) {
        this.shipmentRepository = shipmentRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.itemRepository = itemRepository;
        this.authorizationUtils = authorizationUtils;
    }

    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToDto)
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
        shipment.setDeliveryDate(LocalDateTime.now().plusDays(7));
        shipment.setTradeProposal(tradeProposal);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipmentRepository.save(shipment);
    }

    public ShipmentResponseDto getShipmentById(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeParticipantOrAdmin(shipment);
        return mapToDto(shipment);
    }

    public ShipmentResponseDto updateAddresses(Long id, ShipmentAddressUpdateDto dto) {
        Shipment shipment = loadShipment(id);

        if (shipment.getStatus() != ShipmentStatus.PENDING && shipment.getStatus() != ShipmentStatus.PREPARING) {
            throw new IllegalStateException("No se pueden editar direcciones de un envío " + shipment.getStatus());
        }

        if (dto.getInitiatorAddress() != null) {
            authorizeProposerOrAdmin(shipment);
            shipment.setInitiatorAddress(dto.getInitiatorAddress());
        }
        if (dto.getReceiveAddress() != null) {
            authorizeReceiverOrAdmin(shipment);
            shipment.setReceiveAddress(dto.getReceiveAddress());
        }

        return mapToDto(shipmentRepository.save(shipment));
    }

    public ShipmentResponseDto prepareShipment(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeProposerOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new InvalidShipmentTransitionException("Solo un envío PENDING puede pasar a PREPARING");
        }

        shipment.setStatus(ShipmentStatus.PREPARING);
        shipment.setPreparedAt(LocalDateTime.now());
        return mapToDto(shipmentRepository.save(shipment));
    }

    public ShipmentResponseDto shipShipment(Long id, String trackingCode) {
        Shipment shipment = loadShipment(id);
        authorizeProposerOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.PREPARING) {
            throw new InvalidShipmentTransitionException("Solo un envío PREPARING puede pasar a IN_TRANSIT");
        }

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setShippedAt(LocalDateTime.now());
        if (trackingCode != null && !trackingCode.isBlank()) {
            shipment.setTrackingCode(trackingCode);
        }

        try {
            return mapToDto(shipmentRepository.save(shipment));
        } catch (DataIntegrityViolationException e) {
            throw new InvalidShipmentTransitionException("El código de seguimiento ya está en uso por otro envío");
        }
    }

    @Transactional
    public ShipmentResponseDto deliverShipment(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeReceiverOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidShipmentTransitionException("Solo un envío IN_TRANSIT puede marcarse DELIVERED");
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
        Shipment saved = shipmentRepository.save(shipment);

        completeTradeProposal(saved.getTradeProposal());

        return mapToDto(saved);
    }

    public ShipmentResponseDto cancelShipment(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeParticipantOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.PENDING && shipment.getStatus() != ShipmentStatus.PREPARING) {
            throw new InvalidShipmentTransitionException("Solo un envío PENDING o PREPARING puede cancelarse");
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setCancelledAt(LocalDateTime.now());
        return mapToDto(shipmentRepository.save(shipment));
    }

    public void deleteShipment(Long id) {
        shipmentRepository.deleteById(id);
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

    private Shipment loadShipment(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
    }

    private void authorizeProposerOrAdmin(Shipment shipment) {
        Long proposerId = shipment.getTradeProposal().getProposer().getId();
        if (!authorizationUtils.isAdminOrResourceOwner(proposerId)) {
            throw new ForbiddenOperationException("Solo el proponente o un administrador pueden realizar esta acción");
        }
    }

    private void authorizeReceiverOrAdmin(Shipment shipment) {
        Long receiverId = shipment.getTradeProposal().getReceiver().getId();
        if (!authorizationUtils.isAdminOrResourceOwner(receiverId)) {
            throw new ForbiddenOperationException("Solo el receptor o un administrador pueden realizar esta acción");
        }
    }

    private void authorizeParticipantOrAdmin(Shipment shipment) {
        Long proposerId = shipment.getTradeProposal().getProposer().getId();
        Long receiverId = shipment.getTradeProposal().getReceiver().getId();
        if (!authorizationUtils.isAdminOrResourceOwner(proposerId, receiverId)) {
            throw new ForbiddenOperationException("No tienes permiso sobre este envío");
        }
    }

    private ShipmentResponseDto mapToDto(Shipment shipment) {
        ShipmentResponseDto dto = new ShipmentResponseDto();
        dto.setId(shipment.getId());
        dto.setInitiatorAddress(shipment.getInitiatorAddress());
        dto.setReceiveAddress(shipment.getReceiveAddress());
        dto.setDeliveryDate(shipment.getDeliveryDate());
        dto.setStatus(shipment.getStatus());
        dto.setTrackingCode(shipment.getTrackingCode());
        dto.setCreatedAt(shipment.getCreatedAt());
        dto.setUpdatedAt(shipment.getUpdatedAt());
        dto.setPreparedAt(shipment.getPreparedAt());
        dto.setShippedAt(shipment.getShippedAt());
        dto.setDeliveredAt(shipment.getDeliveredAt());
        dto.setCancelledAt(shipment.getCancelledAt());
        if (shipment.getTradeProposal() != null) {
            dto.setTradeProposalId(shipment.getTradeProposal().getId());
        }
        return dto;
    }
}
