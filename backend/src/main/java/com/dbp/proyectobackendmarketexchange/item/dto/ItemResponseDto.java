package com.dbp.proyectobackendmarketexchange.item.dto;

import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> imageUrls;

    private Long user_id;

    private Long category_id;

    private boolean favorite;

    private String rejectionReason;

    private Long moderatedById;

    private String moderatedByEmail;

    private LocalDateTime moderatedAt;

}
