package com.dbp.proyectobackendmarketexchange.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemImageResponseDto {
    private Long id;
    private Long itemId;
    private String imageUrl;
    private boolean primary;
    private int sortOrder;
    private LocalDateTime createdAt;
}
