package com.dbp.proyectobackendmarketexchange.realtime;

import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageResponseDto;
import com.dbp.proyectobackendmarketexchange.notification.dto.NotificationResponseDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimeMessagingService {
    private static final String NOTIFICATIONS_QUEUE = "/queue/notifications";
    private static final String AGREEMENT_MESSAGES_QUEUE = "/queue/agreement-messages";
    private static final String AGREEMENT_EVENTS_QUEUE = "/queue/agreement-events";

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeMessagingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(Usuario recipient, NotificationResponseDto notification) {
        messagingTemplate.convertAndSendToUser(
                EmailNormalizer.normalize(recipient.getEmail()),
                NOTIFICATIONS_QUEUE,
                notification
        );
    }

    public void sendChatMessage(Usuario recipient, ChatMessageResponseDto message) {
        messagingTemplate.convertAndSendToUser(
                EmailNormalizer.normalize(recipient.getEmail()),
                AGREEMENT_MESSAGES_QUEUE,
                message
        );
    }

    public void sendAgreementEvent(Usuario recipient, TradeProposalResponseDto tradeProposal) {
        messagingTemplate.convertAndSendToUser(
                EmailNormalizer.normalize(recipient.getEmail()),
                AGREEMENT_EVENTS_QUEUE,
                tradeProposal
        );
    }
}
