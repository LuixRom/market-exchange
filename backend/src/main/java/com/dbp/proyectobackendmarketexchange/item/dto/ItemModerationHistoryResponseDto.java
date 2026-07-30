package com.dbp.proyectobackendmarketexchange.item.dto;

import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemModerationHistoryResponseDto {
    private Long id;
    private Long itemId;
    private Long moderatorId;
    private String moderatorEmail;
    private ItemStatus previousStatus;
    private ItemStatus newStatus;
    private String reason;
    private LocalDateTime createdAt;
}
