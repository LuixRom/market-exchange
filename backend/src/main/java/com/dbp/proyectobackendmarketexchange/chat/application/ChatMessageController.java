package com.dbp.proyectobackendmarketexchange.chat.application;

import com.dbp.proyectobackendmarketexchange.chat.domain.ChatMessageService;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageRequestDto;
import com.dbp.proyectobackendmarketexchange.chat.dto.ChatMessageResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agreements/{tradeProposalId}/messages")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping
    public ResponseEntity<List<ChatMessageResponseDto>> listMessages(@PathVariable Long tradeProposalId) {
        return ResponseEntity.ok(chatMessageService.listMessages(tradeProposalId));
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponseDto> sendMessage(@PathVariable Long tradeProposalId,
                                                               @Valid @RequestBody ChatMessageRequestDto request) {
        return new ResponseEntity<>(chatMessageService.sendMessage(tradeProposalId, request), HttpStatus.CREATED);
    }
}
