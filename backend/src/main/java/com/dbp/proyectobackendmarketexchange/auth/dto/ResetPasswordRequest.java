package com.dbp.proyectobackendmarketexchange.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "El token no puede estar vacio")
    private String token;

    @NotBlank(message = "La nueva contrasena no puede estar vacia")
    @Size(min = 8, message = "La nueva contrasena debe tener al menos 8 caracteres")
    private String newPassword;
}
