package com.dbp.proyectobackendmarketexchange.category.dto;

import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
public class CategoryResponseDto {
    private Long id;

    private String name;

    private String description;

}
