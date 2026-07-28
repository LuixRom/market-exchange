package com.dbp.proyectobackendmarketexchange.rating.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class RatingRequestDto {

    @NotNull(message = "El ID del usuario que recibe la calificación es requerido")
    private Long usuarioId; // ID del usuario que está siendo calificado

    @NotNull(message = "El ID del usuario que da la calificación es requerido")
    private Long raterUsuarioId; // ID del usuario que realiza la calificación

    @Min(1)
    @Max(5)
    @NotNull(message = "La calificación es requerida y debe estar entre 1 y 5")
    private int rating; // Calificación

    @NotBlank(message = "El comentario no puede estar vacío")
    private String comment; // Comentario sobre el trueque
}