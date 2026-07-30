package com.dbp.proyectobackendmarketexchange.report.dto;

import com.dbp.proyectobackendmarketexchange.report.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportReviewRequestDto {
    @NotNull(message = "El estado es requerido")
    private ReportStatus status;

    @Size(max = 1000, message = "Las notas no pueden superar 1000 caracteres")
    private String adminNotes;
}
