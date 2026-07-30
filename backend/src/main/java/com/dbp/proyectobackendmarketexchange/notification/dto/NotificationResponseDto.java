package com.dbp.proyectobackendmarketexchange.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDto {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long tradeProposalId;
    private Long itemId;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
