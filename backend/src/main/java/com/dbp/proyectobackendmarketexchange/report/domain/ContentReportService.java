package com.dbp.proyectobackendmarketexchange.report.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.report.dto.ReportRequestDto;
import com.dbp.proyectobackendmarketexchange.report.dto.ReportResponseDto;
import com.dbp.proyectobackendmarketexchange.report.dto.ReportReviewRequestDto;
import com.dbp.proyectobackendmarketexchange.report.infrastructure.ContentReportRepository;
import com.dbp.proyectobackendmarketexchange.tradeproposal.infrastructure.TradeProposalRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentReportService {
    private final ContentReportRepository reportRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemRepository itemRepository;
    private final TradeProposalRepository tradeProposalRepository;
    private final Clock clock;

    public ContentReportService(ContentReportRepository reportRepository,
                                UsuarioRepository usuarioRepository,
                                ItemRepository itemRepository,
                                TradeProposalRepository tradeProposalRepository,
                                Clock clock) {
        this.reportRepository = reportRepository;
        this.usuarioRepository = usuarioRepository;
        this.itemRepository = itemRepository;
        this.tradeProposalRepository = tradeProposalRepository;
        this.clock = clock;
    }

    public ReportResponseDto createReport(ReportRequestDto request) {
        Usuario reporter = currentUser();
        validateTargetExists(request.getTargetType(), request.getTargetId());

        ContentReport report = new ContentReport();
        report.setReporter(reporter);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason().trim());
        report.setDetails(request.getDetails());
        report.setStatus(ReportStatus.PENDING);
        return mapToDto(reportRepository.save(report));
    }

    public List<ReportResponseDto> listReports(ReportStatus status) {
        List<ContentReport> reports = status == null
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusOrderByCreatedAtDesc(status);
        return reports.stream().map(this::mapToDto).toList();
    }

    public ReportResponseDto getReport(Long id) {
        return mapToDto(reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado")));
    }

    public ReportResponseDto reviewReport(Long id, ReportReviewRequestDto request) {
        ContentReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
        Usuario admin = currentUser();
        report.setStatus(request.getStatus());
        report.setAdminNotes(request.getAdminNotes());
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now(clock));
        return mapToDto(reportRepository.save(report));
    }

    private void validateTargetExists(ReportTargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case USER -> usuarioRepository.existsById(targetId);
            case ITEM -> itemRepository.existsById(targetId);
            case TRADE_PROPOSAL -> tradeProposalRepository.existsById(targetId);
        };
        if (!exists) {
            throw new ResourceNotFoundException("Objetivo reportado no encontrado");
        }
    }

    private Usuario currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private ReportResponseDto mapToDto(ContentReport report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(report.getId());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterEmail(report.getReporter().getEmail());
        dto.setReason(report.getReason());
        dto.setDetails(report.getDetails());
        dto.setStatus(report.getStatus());
        dto.setAdminNotes(report.getAdminNotes());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setReviewedAt(report.getReviewedAt());
        if (report.getReviewedBy() != null) {
            dto.setReviewedById(report.getReviewedBy().getId());
            dto.setReviewedByEmail(report.getReviewedBy().getEmail());
        }
        return dto;
    }
}
