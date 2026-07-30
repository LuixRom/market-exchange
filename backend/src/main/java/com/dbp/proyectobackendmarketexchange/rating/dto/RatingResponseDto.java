package com.dbp.proyectobackendmarketexchange.rating.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RatingResponseDto {

    private Long id;
    private Long tradeProposalId;
    private int score;
    private Integer communicationScore;
    private Integer punctualityScore;
    private Integer itemConditionScore;
    private String comment;
    private Long reviewerId;
    private String reviewerName;
    private Long reviewedUserId;
    private String reviewedUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
