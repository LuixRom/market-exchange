package com.dbp.proyectobackendmarketexchange.usuario.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusRequest {
    @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
    private String reason;
}
