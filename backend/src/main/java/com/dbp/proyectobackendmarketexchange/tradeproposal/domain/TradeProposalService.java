package com.dbp.proyectobackendmarketexchange.tradeproposal.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.event.tradeproposal.TradeProposalAcceptedEvent;
import com.dbp.proyectobackendmarketexchange.event.tradeproposal.TradeProposalCreatedEvent;
import com.dbp.proyectobackendmarketexchange.event.tradeproposal.TradeProposalRejectedEvent;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.InvalidTradeProposalException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.TradeProposalConflictException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.notification.domain.NotificationService;
import com.dbp.proyectobackendmarketexchange.realtime.RealtimeMessagingService;
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalRequestDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalSummary;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeProposalService {
    private final ItemRepository itemRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final ShipmentService shipmentService;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthorizationUtils authorizationUtils;
    private final NotificationService notificationService;
    private final RealtimeMessagingService realtimeMessagingService;

    public TradeProposalService(ItemRepository itemRepository,
                                TradeProposalRepository tradeProposalRepository,
                                ShipmentService shipmentService,
                                UsuarioRepository usuarioRepository,
                                ApplicationEventPublisher eventPublisher,
                                AuthorizationUtils authorizationUtils,
                                NotificationService notificationService,
                                RealtimeMessagingService realtimeMessagingService) {
        this.itemRepository = itemRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.shipmentService = shipmentService;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
        this.authorizationUtils = authorizationUtils;
        this.notificationService = notificationService;
        this.realtimeMessagingService = realtimeMessagingService;
    }

    public List<TradeProposalResponseDto> getAllTradeProposals() {
        Usuario current = resolveCurrentUser();
        List<TradeProposal> proposals = current.getRole() == Role.ADMIN
                ? tradeProposalRepository.findAll()
                : tradeProposalRepository.findByProposerIdOrderByCreatedAtDesc(current.getId());
        return proposals.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    public List<TradeProposalResponseDto> getSentTradeProposals() {
        Usuario current = resolveCurrentUser();
        return tradeProposalRepository.findByProposerIdOrderByCreatedAtDesc(current.getId()).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<TradeProposalResponseDto> getReceivedTradeProposals() {
        Usuario current = resolveCurrentUser();
        return tradeProposalRepository.findByReceiverIdOrderByCreatedAtDesc(current.getId()).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public TradeProposalResponseDto getTradeProposalById(Long id) {
        TradeProposal tradeProposal = tradeProposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        authorizeParticipantOrAdmin(tradeProposal);
        return mapToResponseDto(tradeProposal);
    }

    @Transactional
    public TradeProposalResponseDto createTradeProposal(TradeProposalRequestDto requestDto) {
        Usuario proposer = resolveCurrentUser();
        assertActiveUser(proposer);
        if (requestDto.getOfferedItemId().equals(requestDto.getRequestedItemId())) {
            throw new InvalidTradeProposalException("El item ofrecido y el item solicitado no pueden ser el mismo");
        }

        Item offeredItem = itemRepository.findById(requestDto.getOfferedItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item ofrecido no encontrado"));
        Item requestedItem = itemRepository.findById(requestDto.getRequestedItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item solicitado no encontrado"));

        if (!offeredItem.getUsuario().getId().equals(proposer.getId())) {
            throw new InvalidTradeProposalException("No eres el dueno del item ofrecido");
        }
        if (offeredItem.getStatus() != ItemStatus.APPROVED || requestedItem.getStatus() != ItemStatus.APPROVED) {
            throw new InvalidTradeProposalException("Ambos items deben estar APPROVED");
        }
        if (offeredItem.getUsuario().getId().equals(requestedItem.getUsuario().getId())) {
            throw new InvalidTradeProposalException("No puedes proponer un intercambio contigo mismo");
        }
        if (tradeProposalRepository.existsByOfferedItemIdAndRequestedItemIdAndStatus(
                offeredItem.getId(), requestedItem.getId(), TradeStatus.PENDING)) {
            throw new TradeProposalConflictException("Ya existe una propuesta activa para este mismo par de items");
        }

        TradeProposal tradeProposal = new TradeProposal();
        tradeProposal.setOfferedItem(offeredItem);
        tradeProposal.setRequestedItem(requestedItem);
        tradeProposal.setProposer(proposer);
        tradeProposal.setReceiver(requestedItem.getUsuario());
        tradeProposal.setStatus(TradeStatus.PENDING);
        tradeProposal.setInitialMessage(requestDto.getInitialMessage());

        TradeProposal saved;
        try {
            saved = tradeProposalRepository.save(tradeProposal);
        } catch (DataIntegrityViolationException ex) {
            throw new TradeProposalConflictException("Ya existe una propuesta activa para este mismo par de items");
        }

        notificationService.create(saved.getReceiver(), "TRADE_PROPOSAL_CREATED", "Nueva propuesta de intercambio",
                proposer.getEmail() + " quiere intercambiar por tu item " + requestedItem.getName(), saved.getId(), requestedItem.getId());
        notificationService.create(saved.getProposer(), "TRADE_PROPOSAL_SENT", "Propuesta enviada",
                "Tu propuesta de intercambio fue enviada", saved.getId(), offeredItem.getId());
        eventPublisher.publishEvent(new TradeProposalCreatedEvent(this, saved));
        TradeProposalResponseDto response = mapToResponseDto(saved);
        publishProposalUpdate(saved, response);
        return response;
    }

    @Transactional
    public TradeProposalResponseDto acceptTradeProposal(Long id) {
        TradeProposalSummary summary = tradeProposalRepository.findSummaryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        if (!authorizationUtils.isAdminOrResourceOwner(summary.getReceiverId())) {
            throw new ForbiddenOperationException("Solo el receptor o un administrador pueden aceptar esta propuesta");
        }

        long firstItemId = Math.min(summary.getOfferedItemId(), summary.getRequestedItemId());
        long secondItemId = Math.max(summary.getOfferedItemId(), summary.getRequestedItemId());
        itemRepository.findByIdForUpdate(firstItemId).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        itemRepository.findByIdForUpdate(secondItemId).orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        TradeProposal lockedProposal = tradeProposalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        if (lockedProposal.getStatus() != TradeStatus.PENDING) {
            throw new TradeProposalConflictException("La propuesta ya no esta en estado PENDING");
        }
        Item offeredItem = lockedProposal.getOfferedItem();
        Item requestedItem = lockedProposal.getRequestedItem();
        if (offeredItem.getStatus() != ItemStatus.APPROVED || requestedItem.getStatus() != ItemStatus.APPROVED) {
            throw new TradeProposalConflictException("Uno de los items ya no esta disponible");
        }

        offeredItem.setStatus(ItemStatus.RESERVED);
        requestedItem.setStatus(ItemStatus.RESERVED);
        itemRepository.save(offeredItem);
        itemRepository.save(requestedItem);

        lockedProposal.setStatus(TradeStatus.ACCEPTED);
        TradeProposal savedProposal = tradeProposalRepository.save(lockedProposal);
        tradeProposalRepository.cancelCompetingProposals(List.of(offeredItem.getId(), requestedItem.getId()), id);
        shipmentService.createShipmentForTradeProposal(savedProposal);

        notificationService.create(savedProposal.getProposer(), "TRADE_PROPOSAL_ACCEPTED", "Propuesta aceptada",
                "Tu propuesta de intercambio fue aceptada", savedProposal.getId(), requestedItem.getId());
        eventPublisher.publishEvent(new TradeProposalAcceptedEvent(this, savedProposal));
        TradeProposalResponseDto response = mapToResponseDto(savedProposal);
        publishProposalUpdate(savedProposal, response);
        return response;
    }

    @Transactional
    public TradeProposalResponseDto rejectTradeProposal(Long id) {
        TradeProposalSummary summary = tradeProposalRepository.findSummaryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        if (!authorizationUtils.isAdminOrResourceOwner(summary.getReceiverId())) {
            throw new ForbiddenOperationException("Solo el receptor o un administrador pueden rechazar esta propuesta");
        }

        TradeProposal lockedProposal = tradeProposalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        if (lockedProposal.getStatus() != TradeStatus.PENDING) {
            throw new TradeProposalConflictException("La propuesta ya no esta en estado PENDING");
        }
        lockedProposal.setStatus(TradeStatus.REJECTED);
        TradeProposal savedProposal = tradeProposalRepository.save(lockedProposal);
        notificationService.create(savedProposal.getProposer(), "TRADE_PROPOSAL_REJECTED", "Propuesta rechazada",
                "Tu propuesta de intercambio fue rechazada", savedProposal.getId(), savedProposal.getRequestedItem().getId());
        eventPublisher.publishEvent(new TradeProposalRejectedEvent(this, savedProposal));
        TradeProposalResponseDto response = mapToResponseDto(savedProposal);
        publishProposalUpdate(savedProposal, response);
        return response;
    }

    @Transactional
    public TradeProposalResponseDto cancelTradeProposal(Long id) {
        TradeProposal tradeProposal = tradeProposalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        Usuario current = resolveCurrentUser();
        if (!tradeProposal.getProposer().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Solo el proponente o un admin pueden cancelar esta propuesta");
        }
        if (tradeProposal.getStatus() != TradeStatus.PENDING) {
            throw new TradeProposalConflictException("Solo se pueden cancelar propuestas PENDING");
        }
        tradeProposal.setStatus(TradeStatus.CANCELLED);
        tradeProposal.setCancelledAt(LocalDateTime.now());
        TradeProposal saved = tradeProposalRepository.save(tradeProposal);
        notificationService.create(saved.getReceiver(), "TRADE_PROPOSAL_CANCELLED", "Propuesta cancelada",
                "Una propuesta de intercambio fue cancelada", saved.getId(), saved.getRequestedItem().getId());
        TradeProposalResponseDto response = mapToResponseDto(saved);
        publishProposalUpdate(saved, response);
        return response;
    }

    public void deleteTradeProposal(Long id) {
        TradeProposal tradeProposal = tradeProposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        if (tradeProposal.getShipment() != null) {
            throw new InvalidTradeProposalException("No se puede eliminar una propuesta que ya tiene un envio asociado");
        }
        tradeProposalRepository.delete(tradeProposal);
    }

    private void authorizeParticipantOrAdmin(TradeProposal tradeProposal) {
        Usuario current = resolveCurrentUser();
        if (current.getRole() == Role.ADMIN) {
            return;
        }
        if (!tradeProposal.getProposer().getId().equals(current.getId())
                && !tradeProposal.getReceiver().getId().equals(current.getId())) {
            throw new ForbiddenOperationException("No tienes permiso sobre esta propuesta");
        }
    }

    private Usuario resolveCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private TradeProposalResponseDto mapToResponseDto(TradeProposal tradeProposal) {
        TradeProposalResponseDto dto = new TradeProposalResponseDto();
        dto.setId(tradeProposal.getId());
        dto.setStatus(tradeProposal.getStatus());
        dto.setOfferedItemId(tradeProposal.getOfferedItem().getId());
        dto.setOfferedItemName(tradeProposal.getOfferedItem().getName());
        dto.setRequestedItemId(tradeProposal.getRequestedItem().getId());
        dto.setRequestedItemName(tradeProposal.getRequestedItem().getName());
        dto.setProposerId(tradeProposal.getProposer().getId());
        dto.setProposerEmail(tradeProposal.getProposer().getEmail());
        dto.setReceiverId(tradeProposal.getReceiver().getId());
        dto.setReceiverEmail(tradeProposal.getReceiver().getEmail());
        dto.setInitialMessage(tradeProposal.getInitialMessage());
        dto.setCreatedAt(tradeProposal.getCreatedAt());
        dto.setUpdatedAt(tradeProposal.getUpdatedAt());
        dto.setCancelledAt(tradeProposal.getCancelledAt());
        return dto;
    }

    private void publishProposalUpdate(TradeProposal tradeProposal, TradeProposalResponseDto response) {
        realtimeMessagingService.sendAgreementEvent(tradeProposal.getProposer(), response);
        realtimeMessagingService.sendAgreementEvent(tradeProposal.getReceiver(), response);
    }

    private void assertActiveUser(Usuario usuario) {
        if (usuario.isBlocked() || usuario.isSuspended()) {
            throw new ForbiddenOperationException("Tu cuenta no puede crear propuestas en este momento");
        }
    }
}
