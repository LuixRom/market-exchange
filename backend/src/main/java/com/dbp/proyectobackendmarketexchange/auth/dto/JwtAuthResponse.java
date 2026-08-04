package com.dbp.proyectobackendmarketexchange.auth.dto;

import lombok.Data;

@Data
public class JwtAuthResponse {
    private String token;
    private String refreshToken;
    private boolean emailVerified;
}
