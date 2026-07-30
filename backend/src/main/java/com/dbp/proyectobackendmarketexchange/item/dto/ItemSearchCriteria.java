package com.dbp.proyectobackendmarketexchange.item.dto;

import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import lombok.Data;

@Data
public class ItemSearchCriteria {
    private Long categoryId;
    private Long userId;
    private Condition condition;
    private ItemStatus status;
    private String q;
}
