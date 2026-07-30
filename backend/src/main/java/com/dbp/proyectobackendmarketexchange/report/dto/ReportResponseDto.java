package com.dbp.proyectobackendmarketexchange.report.dto;

import com.dbp.proyectobackendmarketexchange.report.domain.ReportStatus;
import com.dbp.proyectobackendmarketexchange.report.domain.ReportTargetType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportResponseDto {
    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private Long reporterId;
    private String reporterEmail;
    private String reason;
    private String details;
    private ReportStatus status;
    private String adminNotes;
    private Long reviewedById;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
