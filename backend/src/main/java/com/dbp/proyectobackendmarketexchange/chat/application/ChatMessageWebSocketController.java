package com.dbp.proyectobackendmarketexchange.chat.application;

import com.dbp.proyectobackendmarketexchange.chat.domain.ChatMessageService;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageRequestDto;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageResponseDto;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatMessageWebSocketController {
    private final ChatMessageService chatMessageService;

    public ChatMessageWebSocketController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/agreements/{tradeProposalId}/messages")
    public ChatMessageResponseDto sendMessage(@DestinationVariable Long tradeProposalId,
                                               @Valid @Payload ChatMessageRequestDto request,
                                               Principal principal) {
        return chatMessageService.sendMessageAsUser(tradeProposalId, request, principal.getName());
    }
}
