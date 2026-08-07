package com.dbp.proyectobackendmarketexchange.item.dto;

import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class ItemRequestDto {
    @NotBlank(message = "El nombre no puede estar vacio")
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @JsonProperty("category_id")
    private Long categoryId;

    @NotNull
    @JsonProperty("user_id")
    private Long userId;

    @NotNull
    private Condition condition;


    private MultipartFile image;

    private List<MultipartFile> images;

}
