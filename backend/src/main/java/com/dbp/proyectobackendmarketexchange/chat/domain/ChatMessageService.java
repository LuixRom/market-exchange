package com.dbp.proyectobackendmarketexchange.chat.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageRequestDto;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageResponseDto;
import com.dbp.proyectobackendmarketexchange.chat.infrastructure.ChatMessageRepository;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.notification.domain.NotificationService;
import com.dbp.proyectobackendmarketexchange.realtime.RealtimeMessagingService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationService notificationService;
    private final RealtimeMessagingService realtimeMessagingService;
    private final Clock clock;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              TradeProposalRepository tradeProposalRepository,
                              UsuarioRepository usuarioRepository,
                              NotificationService notificationService,
                              RealtimeMessagingService realtimeMessagingService,
                              Clock clock) {
        this.chatMessageRepository = chatMessageRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificationService = notificationService;
        this.realtimeMessagingService = realtimeMessagingService;
        this.clock = clock;
    }

    public List<ChatMessageResponseDto> listMessages(Long tradeProposalId) {
        TradeProposal tradeProposal = loadParticipantProposal(tradeProposalId);
        Usuario current = currentUser();
        List<ChatMessage> messages = chatMessageRepository.findByTradeProposalIdOrderByCreatedAtAsc(tradeProposal.getId());
        messages.stream()
                .filter(message -> !message.getSender().getId().equals(current.getId()) && message.getReadAt() == null)
                .forEach(message -> message.setReadAt(LocalDateTime.now(clock)));
        chatMessageRepository.saveAll(messages);
        return messages.stream().map(this::mapToDto).toList();
    }

    public ChatMessageResponseDto sendMessage(Long tradeProposalId, ChatMessageRequestDto request) {
        return sendMessageAsUser(tradeProposalId, request, currentUserEmail());
    }

    public ChatMessageResponseDto sendMessageAsUser(Long tradeProposalId, ChatMessageRequestDto request, String senderEmail) {
        Usuario current = loadUserByEmail(senderEmail);
        TradeProposal tradeProposal = loadParticipantProposal(tradeProposalId, current);

        ChatMessage message = new ChatMessage();
        message.setTradeProposal(tradeProposal);
        message.setSender(current);
        message.setContent(request.getContent().trim());
        message = chatMessageRepository.save(message);
        ChatMessageResponseDto response = mapToDto(message);

        Usuario recipient = tradeProposal.getProposer().getId().equals(current.getId())
                ? tradeProposal.getReceiver()
                : tradeProposal.getProposer();
        notificationService.create(recipient, "CHAT_MESSAGE", "Nuevo mensaje",
                "Tienes un nuevo mensaje sobre una propuesta de intercambio", tradeProposal.getId(), null);

        realtimeMessagingService.sendChatMessage(current, response);
        realtimeMessagingService.sendChatMessage(recipient, response);
        realtimeMessagingService.sendChatMessageToAgreement(response);

        return response;
    }

    private TradeProposal loadParticipantProposal(Long tradeProposalId) {
        return loadParticipantProposal(tradeProposalId, currentUser());
    }

    private TradeProposal loadParticipantProposal(Long tradeProposalId, Usuario current) {
        TradeProposal tradeProposal = tradeProposalRepository.findById(tradeProposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade proposal not found"));
        if (!tradeProposal.getProposer().getId().equals(current.getId())
                && !tradeProposal.getReceiver().getId().equals(current.getId())) {
            throw new ForbiddenOperationException("No tienes permiso para ver este chat");
        }
        return tradeProposal;
    }

    private String currentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        return ((UserDetails) principal).getUsername();
    }

    private Usuario currentUser() {
        return loadUserByEmail(currentUserEmail());
    }

    private Usuario loadUserByEmail(String email) {
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private ChatMessageResponseDto mapToDto(ChatMessage message) {
        ChatMessageResponseDto dto = new ChatMessageResponseDto();
        dto.setId(message.getId());
        dto.setTradeProposalId(message.getTradeProposal().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderEmail(message.getSender().getEmail());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadAt(message.getReadAt());
        return dto;
    }
}
