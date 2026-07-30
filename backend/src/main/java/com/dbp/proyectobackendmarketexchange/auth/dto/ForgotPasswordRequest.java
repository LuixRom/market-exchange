package com.dbp.proyectobackendmarketexchange.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "El correo debe ser valido")
    @NotBlank(message = "El correo no puede estar vacio")
    private String email;
}
