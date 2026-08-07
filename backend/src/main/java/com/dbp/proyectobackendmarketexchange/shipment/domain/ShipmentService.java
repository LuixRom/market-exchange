package com.dbp.proyectobackendmarketexchange.shipment.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.InvalidShipmentTransitionException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.notification.domain.NotificationService;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentAddressUpdateDto;
import com.dbp.proyectobackendmarketexchange.shipment.dto.ShipmentResponseDto;
import com.dbp.proyectobackendmarketexchange.shipment.infrastructure.ShipmentRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final ItemRepository itemRepository;
    private final AuthorizationUtils authorizationUtils;
    private final NotificationService notificationService;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           TradeProposalRepository tradeProposalRepository,
                           ItemRepository itemRepository,
                           AuthorizationUtils authorizationUtils,
                           NotificationService notificationService,
                           UsuarioRepository usuarioRepository,
                           Clock clock) {
        this.shipmentRepository = shipmentRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.itemRepository = itemRepository;
        this.authorizationUtils = authorizationUtils;
        this.notificationService = notificationService;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    public List<ShipmentResponseDto> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public void createShipmentForTradeProposal(TradeProposal tradeProposal) {
        if (tradeProposal.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalStateException("La propuesta debe estar en estado ACCEPTED para crear un envio");
        }

        if (shipmentRepository.existsByTradeProposalId(tradeProposal.getId())) {
            return;
        }

        Shipment shipment = new Shipment();
        shipment.setInitiatorAddress(tradeProposal.getProposer().getAddress());
        shipment.setReceiveAddress(tradeProposal.getReceiver().getAddress());
        shipment.setDeliveryDate(LocalDateTime.now(clock).plusDays(7));
        shipment.setTradeProposal(tradeProposal);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setMethod(ShipmentMethod.EXTERNAL_SHIPPING);
        Shipment saved = shipmentRepository.save(shipment);

        notifyBoth(saved, "SHIPMENT_CREATED", "Entrega creada",
                "Ya puedes coordinar la entrega del intercambio aceptado");
    }

    public ShipmentResponseDto getShipmentById(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeParticipantOrAdmin(shipment);
        return mapToDto(shipment);
    }

    public ShipmentResponseDto getShipmentByTradeProposalId(Long tradeProposalId) {
        Shipment shipment = shipmentRepository.findByTradeProposalId(tradeProposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        authorizeParticipantOrAdmin(shipment);
        return mapToDto(shipment);
    }

    public ShipmentResponseDto updateAddresses(Long id, ShipmentAddressUpdateDto dto) {
        Shipment shipment = loadShipment(id);
        authorizeParticipantOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.PENDING && shipment.getStatus() != ShipmentStatus.PREPARING) {
            throw new IllegalStateException("No se puede editar una entrega " + shipment.getStatus());
        }

        if (dto.getInitiatorAddress() != null) {
            authorizeProposerOrAdmin(shipment);
            shipment.setInitiatorAddress(dto.getInitiatorAddress());
        }
        if (dto.getReceiveAddress() != null) {
            authorizeReceiverOrAdmin(shipment);
            shipment.setReceiveAddress(dto.getReceiveAddress());
        }
        if (dto.getDeliveryDate() != null) {
            shipment.setDeliveryDate(dto.getDeliveryDate());
        }
        if (dto.getMethod() != null) {
            shipment.setMethod(dto.getMethod());
            if (dto.getMethod() == ShipmentMethod.IN_PERSON) {
                shipment.setTrackingCode(null);
            }
        }

        Shipment saved = shipmentRepository.save(shipment);
        notifyOtherParticipant(saved, currentUser(), "SHIPMENT_UPDATED", "Entrega actualizada",
                "La informacion de entrega del intercambio fue actualizada");
        return mapToDto(saved);
    }

    public ShipmentResponseDto prepareShipment(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeProposerOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.PENDING) {
            throw new InvalidShipmentTransitionException("Solo una entrega PENDING puede pasar a PREPARING");
        }

        shipment.setStatus(ShipmentStatus.PREPARING);
        shipment.setPreparedAt(LocalDateTime.now(clock));
        Shipment saved = shipmentRepository.save(shipment);
        notifyOtherParticipant(saved, currentUser(), "SHIPMENT_PREPARING", "Entrega en preparacion",
                "La otra parte esta preparando la entrega");
        return mapToDto(saved);
    }

    public ShipmentResponseDto shipShipment(Long id, String trackingCode) {
        Shipment shipment = loadShipment(id);
        authorizeProposerOrAdmin(shipment);

        if (shipment.getMethod() == ShipmentMethod.IN_PERSON) {
            throw new InvalidShipmentTransitionException("Una entrega presencial no necesita pasar a IN_TRANSIT");
        }
        if (shipment.getStatus() != ShipmentStatus.PREPARING) {
            throw new InvalidShipmentTransitionException("Solo una entrega PREPARING puede pasar a IN_TRANSIT");
        }

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setShippedAt(LocalDateTime.now(clock));
        if (trackingCode != null && !trackingCode.isBlank()) {
            shipment.setTrackingCode(trackingCode);
        }

        try {
            Shipment saved = shipmentRepository.save(shipment);
            notifyOtherParticipant(saved, currentUser(), "SHIPMENT_IN_TRANSIT", "Entrega en camino",
                    "La entrega del intercambio esta en camino");
            return mapToDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidShipmentTransitionException("El codigo de seguimiento ya esta en uso por otra entrega");
        }
    }

    @Transactional
    public ShipmentResponseDto deliverShipment(Long id) {
        return doConfirmDelivery(id);
    }

    @Transactional
    public ShipmentResponseDto confirmDelivery(Long id) {
        return doConfirmDelivery(id);
    }

    private ShipmentResponseDto doConfirmDelivery(Long id) {
        Shipment shipment = loadShipment(id);
        Usuario current = currentUser();
        authorizeParticipantOrAdmin(shipment);

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new InvalidShipmentTransitionException("No se puede confirmar una entrega cancelada");
        }
        if (shipment.getTradeProposal().getStatus() == TradeStatus.COMPLETED) {
            return mapToDto(shipment);
        }
        if (shipment.getMethod() == ShipmentMethod.EXTERNAL_SHIPPING
                && shipment.getStatus() != ShipmentStatus.IN_TRANSIT
                && shipment.getStatus() != ShipmentStatus.DELIVERED) {
            throw new InvalidShipmentTransitionException("Una entrega externa debe estar IN_TRANSIT para confirmarse");
        }

        boolean proposer = shipment.getTradeProposal().getProposer().getId().equals(current.getId());
        if (proposer && shipment.getProposerDeliveryConfirmedAt() == null) {
            shipment.setProposerDeliveryConfirmedAt(LocalDateTime.now(clock));
        } else if (!proposer && shipment.getReceiverDeliveryConfirmedAt() == null) {
            shipment.setReceiverDeliveryConfirmedAt(LocalDateTime.now(clock));
        }

        if (shipment.getDeliveredAt() == null) {
            shipment.setDeliveredAt(LocalDateTime.now(clock));
        }
        shipment.setStatus(ShipmentStatus.DELIVERED);

        Shipment saved = shipmentRepository.save(shipment);
        notifyOtherParticipant(saved, current, "SHIPMENT_DELIVERY_CONFIRMED", "Entrega confirmada",
                "La otra parte confirmo la entrega del intercambio");

        if (isDeliveryConfirmedByBoth(saved)) {
            completeTradeProposal(saved.getTradeProposal());
            notifyBoth(saved, "TRADE_COMPLETED", "Intercambio completado",
                    "Ambas partes confirmaron la entrega. Ya pueden calificarse");
        }

        return mapToDto(saved);
    }

    public ShipmentResponseDto cancelShipment(Long id) {
        Shipment shipment = loadShipment(id);
        authorizeParticipantOrAdmin(shipment);

        if (shipment.getStatus() != ShipmentStatus.PENDING && shipment.getStatus() != ShipmentStatus.PREPARING) {
            throw new InvalidShipmentTransitionException("Solo una entrega PENDING o PREPARING puede cancelarse");
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setCancelledAt(LocalDateTime.now(clock));
        Shipment saved = shipmentRepository.save(shipment);
        notifyOtherParticipant(saved, currentUser(), "SHIPMENT_CANCELLED", "Entrega cancelada",
                "La entrega del intercambio fue cancelada");
        return mapToDto(saved);
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
            throw new ForbiddenOperationException("Solo el proponente o un administrador pueden realizar esta accion");
        }
    }

    private void authorizeReceiverOrAdmin(Shipment shipment) {
        Long receiverId = shipment.getTradeProposal().getReceiver().getId();
        if (!authorizationUtils.isAdminOrResourceOwner(receiverId)) {
            throw new ForbiddenOperationException("Solo el receptor o un administrador pueden realizar esta accion");
        }
    }

    private void authorizeParticipantOrAdmin(Shipment shipment) {
        Long proposerId = shipment.getTradeProposal().getProposer().getId();
        Long receiverId = shipment.getTradeProposal().getReceiver().getId();
        if (!authorizationUtils.isAdminOrResourceOwner(proposerId, receiverId)) {
            throw new ForbiddenOperationException("No tienes permiso sobre esta entrega");
        }
    }

    private Usuario currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private void notifyOtherParticipant(Shipment shipment, Usuario actor, String type, String title, String message) {
        TradeProposal tradeProposal = shipment.getTradeProposal();
        Usuario recipient = tradeProposal.getProposer().getId().equals(actor.getId())
                ? tradeProposal.getReceiver()
                : tradeProposal.getProposer();
        notificationService.create(recipient, type, title, message, tradeProposal.getId(), null);
    }

    private void notifyBoth(Shipment shipment, String type, String title, String message) {
        TradeProposal tradeProposal = shipment.getTradeProposal();
        notificationService.create(tradeProposal.getProposer(), type, title, message, tradeProposal.getId(), null);
        notificationService.create(tradeProposal.getReceiver(), type, title, message, tradeProposal.getId(), null);
    }

    private boolean isDeliveryConfirmedByBoth(Shipment shipment) {
        return shipment.getProposerDeliveryConfirmedAt() != null
                && shipment.getReceiverDeliveryConfirmedAt() != null;
    }

    private ShipmentResponseDto mapToDto(Shipment shipment) {
        ShipmentResponseDto dto = new ShipmentResponseDto();
        dto.setId(shipment.getId());
        dto.setInitiatorAddress(shipment.getInitiatorAddress());
        dto.setReceiveAddress(shipment.getReceiveAddress());
        dto.setDeliveryDate(shipment.getDeliveryDate());
        dto.setStatus(shipment.getStatus());
        dto.setMethod(shipment.getMethod());
        dto.setTrackingCode(shipment.getTrackingCode());
        dto.setCreatedAt(shipment.getCreatedAt());
        dto.setUpdatedAt(shipment.getUpdatedAt());
        dto.setPreparedAt(shipment.getPreparedAt());
        dto.setShippedAt(shipment.getShippedAt());
        dto.setDeliveredAt(shipment.getDeliveredAt());
        dto.setCancelledAt(shipment.getCancelledAt());
        dto.setProposerDeliveryConfirmedAt(shipment.getProposerDeliveryConfirmedAt());
        dto.setReceiverDeliveryConfirmedAt(shipment.getReceiverDeliveryConfirmedAt());
        dto.setDeliveryConfirmedByBoth(isDeliveryConfirmedByBoth(shipment));
        if (shipment.getTradeProposal() != null) {
            dto.setTradeProposalId(shipment.getTradeProposal().getId());
        }
        return dto;
    }
}
