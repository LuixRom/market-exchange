package com.dbp.proyectobackendmarketexchange.usuario.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @Size(max = 255, message = "La bio no puede tener mas de 255 caracteres")
    private String bio;

    @Size(max = 255, message = "La URL del avatar no puede tener mas de 255 caracteres")
    private String avatarUrl;

    @Size(max = 100, message = "La ubicacion no puede tener mas de 100 caracteres")
    private String location;
}
