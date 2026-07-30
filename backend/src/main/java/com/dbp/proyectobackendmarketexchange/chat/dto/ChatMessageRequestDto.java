package com.dbp.proyectobackendmarketexchange.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageRequestDto {
    @NotBlank(message = "El mensaje no puede estar vacio")
    @Size(max = 1000, message = "El mensaje no puede tener mas de 1000 caracteres")
    private String content;
}
