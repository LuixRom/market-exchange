package com.dbp.proyectobackendmarketexchange.report.domain;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "content_report")
public class ContentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @NotNull
    private Long targetId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    @NotNull
    private Usuario reporter;

    @NotBlank
    @Size(max = 120)
    private String reason;

    @Size(max = 1000)
    private String details;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    @Size(max = 1000)
    private String adminNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by_id")
    private Usuario reviewedBy;

    private LocalDateTime reviewedAt;

    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(Clock.systemDefaultZone());
    }
}
