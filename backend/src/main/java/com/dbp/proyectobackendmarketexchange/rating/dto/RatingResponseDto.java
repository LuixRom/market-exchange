package com.dbp.proyectobackendmarketexchange.rating.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RatingResponseDto {

    private Long id;
    private int rating;
    private String comment;
    private String raterUsuarioNombre;
    private String usuarioNombre; // Usuario que recibe la calificación
    private LocalDateTime createdAt;
}