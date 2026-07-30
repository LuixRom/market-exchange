package com.dbp.proyectobackendmarketexchange.report.dto;

import com.dbp.proyectobackendmarketexchange.report.domain.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDto {
    @NotNull(message = "El tipo de objetivo es requerido")
    private ReportTargetType targetType;

    @NotNull(message = "El ID del objetivo es requerido")
    private Long targetId;

    @NotBlank(message = "El motivo es requerido")
    @Size(max = 120, message = "El motivo no puede superar 120 caracteres")
    private String reason;

    @Size(max = 1000, message = "El detalle no puede superar 1000 caracteres")
    private String details;
}
