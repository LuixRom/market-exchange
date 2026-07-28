package com.dbp.proyectobackendmarketexchange.item.dto;

import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ItemRequestDto {
    @NotBlank(message = "El nombre no puede estar vacio")
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Long category_id;

    @NotNull
    private Long user_id;

    @NotNull
    private Condition condition;


    private MultipartFile image;


}
