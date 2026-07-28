package com.dbp.proyectobackendmarketexchange.rating.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingReputationDto {
    private Long userId;
    private Double averageScore;
    private Long ratingCount;
}
