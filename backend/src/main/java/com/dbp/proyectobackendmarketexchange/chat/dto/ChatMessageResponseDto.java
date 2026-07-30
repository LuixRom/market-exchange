package com.dbp.proyectobackendmarketexchange.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponseDto {
    private Long id;
    private Long tradeProposalId;
    private Long senderId;
    private String senderEmail;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
