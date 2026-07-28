package com.dbp.proyectobackendmarketexchange.usuario.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class UsuarioRequestDto {


    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String firstname;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede tener más de 50 caracteres")
    private String lastname;

    @Email(message = "El correo debe ser válido")
    @NotBlank(message = "El correo no puede estar vacío")
    private String email;

    @NotNull(message = "El teléfono no puede estar vacío")
    @Size(min = 7, max = 15, message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String phone;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 100, message = "La dirección no puede tener más de 100 caracteres")
    private String address;
}
