package com.dbp.proyectobackendmarketexchange.tradeproposal.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
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
import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalRequestDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalSummary;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TradeProposalService(ItemRepository itemRepository, TradeProposalRepository tradeProposalRepository,
                                 ShipmentService shipmentService, UsuarioRepository usuarioRepository,
                                 ApplicationEventPublisher eventPublisher, AuthorizationUtils authorizationUtils) {
        this.itemRepository = itemRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.shipmentService = shipmentService;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
        this.authorizationUtils = authorizationUtils;
    }

    public List<TradeProposalResponseDto> getAllTradeProposals() {
        return tradeProposalRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public TradeProposalResponseDto getTradeProposalById(Long id) {
        TradeProposal tradeProposal = tradeProposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        return mapToResponseDto(tradeProposal);
    }

    @Transactional
    public TradeProposalResponseDto createTradeProposal(TradeProposalRequestDto requestDto) {
        Usuario proposer = resolveCurrentUser();

        if (requestDto.getOfferedItemId().equals(requestDto.getRequestedItemId())) {
            throw new InvalidTradeProposalException("El item ofrecido y el item solicitado no pueden ser el mismo");
        }

        Item offeredItem = itemRepository.findById(requestDto.getOfferedItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item ofrecido no encontrado"));
        Item requestedItem = itemRepository.findById(requestDto.getRequestedItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item solicitado no encontrado"));

        if (!offeredItem.getUsuario().getId().equals(proposer.getId())) {
            throw new InvalidTradeProposalException("No eres el dueño del item ofrecido");
        }
        if (offeredItem.getStatus() != ItemStatus.APPROVED) {
            throw new InvalidTradeProposalException("El item ofrecido no está disponible (debe estar APPROVED)");
        }
        if (requestedItem.getStatus() != ItemStatus.APPROVED) {
            throw new InvalidTradeProposalException("El item solicitado no está disponible (debe estar APPROVED)");
        }
        if (offeredItem.getUsuario().getId().equals(requestedItem.getUsuario().getId())) {
            throw new InvalidTradeProposalException("No puedes proponer un intercambio contigo mismo");
        }
        if (tradeProposalRepository.existsByOfferedItemIdAndRequestedItemIdAndStatus(
                offeredItem.getId(), requestedItem.getId(), TradeStatus.PENDING)) {
            throw new TradeProposalConflictException("Ya existe una propuesta activa para este mismo par de items");
        }

        Usuario receiver = requestedItem.getUsuario();

        TradeProposal tradeProposal = new TradeProposal();
        tradeProposal.setOfferedItem(offeredItem);
        tradeProposal.setRequestedItem(requestedItem);
        tradeProposal.setProposer(proposer);
        tradeProposal.setReceiver(receiver);
        tradeProposal.setStatus(TradeStatus.PENDING);

        TradeProposal saved;
        try {
            saved = tradeProposalRepository.save(tradeProposal);
        } catch (DataIntegrityViolationException ex) {
            throw new TradeProposalConflictException("Ya existe una propuesta activa para este mismo par de items");
        }

        eventPublisher.publishEvent(new TradeProposalCreatedEvent(this, saved));

        return mapToResponseDto(saved);
    }

    @Transactional
    public TradeProposalResponseDto acceptTradeProposal(Long id) {
        // Proyección liviana (sin cargar entidades completas): si cargáramos el
        // TradeProposal/Item aquí quedarían gestionados en el contexto de persistencia
        // y el findByIdForUpdate posterior devolvería esa MISMA instancia cacheada en
        // vez de reflejar el estado recién confirmado por quien ganó la carrera.
        TradeProposalSummary summary = tradeProposalRepository.findSummaryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));

        if (!authorizationUtils.isAdminOrResourceOwner(summary.getReceiverId())) {
            throw new ForbiddenOperationException("Solo el receptor o un administrador pueden aceptar esta propuesta");
        }

        // Orden de bloqueo global (por id ascendente) para evitar deadlocks entre dos
        // accepts concurrentes que compartan un item: primero los items, luego la propuesta.
        long firstItemId = Math.min(summary.getOfferedItemId(), summary.getRequestedItemId());
        long secondItemId = Math.max(summary.getOfferedItemId(), summary.getRequestedItemId());

        itemRepository.findByIdForUpdate(firstItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        itemRepository.findByIdForUpdate(secondItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        TradeProposal lockedProposal = tradeProposalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));

        if (lockedProposal.getStatus() == TradeStatus.CANCELLED) {
            throw new TradeProposalConflictException("La propuesta fue cancelada porque una propuesta competidora fue aceptada primero");
        }
        if (lockedProposal.getStatus() != TradeStatus.PENDING) {
            throw new TradeProposalConflictException("La propuesta ya no está en estado PENDING");
        }

        Item offeredItem = lockedProposal.getOfferedItem();
        Item requestedItem = lockedProposal.getRequestedItem();
        if (offeredItem.getStatus() != ItemStatus.APPROVED || requestedItem.getStatus() != ItemStatus.APPROVED) {
            throw new TradeProposalConflictException("Uno de los items ya no está disponible");
        }

        offeredItem.setStatus(ItemStatus.RESERVED);
        requestedItem.setStatus(ItemStatus.RESERVED);
        itemRepository.save(offeredItem);
        itemRepository.save(requestedItem);

        lockedProposal.setStatus(TradeStatus.ACCEPTED);
        TradeProposal savedProposal = tradeProposalRepository.save(lockedProposal);

        tradeProposalRepository.cancelCompetingProposals(
                List.of(offeredItem.getId(), requestedItem.getId()), id);

        shipmentService.createShipmentForTradeProposal(savedProposal);

        eventPublisher.publishEvent(new TradeProposalAcceptedEvent(this, savedProposal));

        return mapToResponseDto(savedProposal);
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
            throw new TradeProposalConflictException("La propuesta ya no está en estado PENDING");
        }

        lockedProposal.setStatus(TradeStatus.REJECTED);
        TradeProposal savedProposal = tradeProposalRepository.save(lockedProposal);

        eventPublisher.publishEvent(new TradeProposalRejectedEvent(this, savedProposal));

        return mapToResponseDto(savedProposal);
    }

    public void deleteTradeProposal(Long id) {
        TradeProposal tradeProposal = tradeProposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));

        if (tradeProposal.getShipment() != null) {
            throw new InvalidTradeProposalException("No se puede eliminar una propuesta que ya tiene un envío asociado");
        }

        tradeProposalRepository.delete(tradeProposal);
    }

    private Usuario resolveCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(email)
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
        return dto;
    }
}
