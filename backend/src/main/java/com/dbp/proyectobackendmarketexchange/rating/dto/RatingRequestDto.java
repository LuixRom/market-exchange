package com.dbp.proyectobackendmarketexchange.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequestDto {

    @NotNull(message = "El ID de la propuesta de intercambio es requerido")
    private Long tradeProposalId;

    @Min(value = 1, message = "La calificacion debe estar entre 1 y 5")
    @Max(value = 5, message = "La calificacion debe estar entre 1 y 5")
    private int score;

    @Min(value = 1, message = "La comunicacion debe estar entre 1 y 5")
    @Max(value = 5, message = "La comunicacion debe estar entre 1 y 5")
    private Integer communicationScore;

    @Min(value = 1, message = "La puntualidad debe estar entre 1 y 5")
    @Max(value = 5, message = "La puntualidad debe estar entre 1 y 5")
    private Integer punctualityScore;

    @Min(value = 1, message = "El estado del producto debe estar entre 1 y 5")
    @Max(value = 5, message = "El estado del producto debe estar entre 1 y 5")
    private Integer itemConditionScore;

    @Size(max = 500, message = "El comentario no puede tener mas de 500 caracteres")
    private String comment;
}
