package com.dbp.proyectobackendmarketexchange.usuario.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponseDto {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String address;
    private String phone;
    private String role;
    private boolean emailVerified;
    private String bio;
    private String avatarUrl;
    private String location;
    private LocalDateTime createdAt;
    private boolean blocked;
    private LocalDateTime blockedAt;
    private String blockedReason;
    private boolean suspended;
    private LocalDateTime suspendedAt;
    private String suspensionReason;
}
