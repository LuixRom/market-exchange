package com.dbp.proyectobackendmarketexchange.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank(message = "El correo no puede estar vacio")
    private String username;

    @NotBlank(message = "La contrasena no puede estar vacia")
    private String password;

}
