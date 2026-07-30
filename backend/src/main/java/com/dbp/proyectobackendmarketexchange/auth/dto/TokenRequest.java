package com.dbp.proyectobackendmarketexchange.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRequest {
    @NotBlank(message = "El token no puede estar vacio")
    private String token;
}
