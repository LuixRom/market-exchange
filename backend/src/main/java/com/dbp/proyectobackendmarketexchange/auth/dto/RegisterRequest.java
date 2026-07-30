package com.dbp.proyectobackendmarketexchange.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "El nombre no puede estar vacio")
    @Size(max = 50, message = "El nombre no puede tener mas de 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacio")
    @Size(max = 50, message = "El apellido no puede tener mas de 50 caracteres")
    private String lastName;

    @Email(message = "El correo debe ser valido")
    @NotBlank(message = "El correo no puede estar vacio")
    private String email;

    @NotBlank(message = "La contrasena no puede estar vacia")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El telefono no puede estar vacio")
    @Size(min = 7, max = 15, message = "El telefono debe tener entre 7 y 15 digitos")
    private String phone;

    @NotBlank(message = "La direccion no puede estar vacia")
    @Size(max = 100, message = "La direccion no puede tener mas de 100 caracteres")
    private String address;
}
