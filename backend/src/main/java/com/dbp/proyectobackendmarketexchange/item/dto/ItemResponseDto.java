package com.dbp.proyectobackendmarketexchange.item.dto;

import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemResponseDto {
    private Long id;

    private String name;

    private String description;

    private String categoryName;

    private Condition condition;

    private String userName;

    private LocalDateTime createdAt;

    private ItemStatus status;

    private String imageUrl;

    private Long user_id;


}
