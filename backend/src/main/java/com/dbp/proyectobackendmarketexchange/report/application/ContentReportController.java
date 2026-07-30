package com.dbp.proyectobackendmarketexchange.report.application;

import com.dbp.proyectobackendmarketexchange.report.domain.ContentReportService;
import com.dbp.proyectobackendmarketexchange.report.domain.ReportStatus;
import com.dbp.proyectobackendmarketexchange.report.dto.ReportRequestDto;
import com.dbp.proyectobackendmarketexchange.report.dto.ReportResponseDto;
import com.dbp.proyectobackendmarketexchange.report.dto.ReportReviewRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContentReportController {
    private final ContentReportService reportService;

    public ContentReportController(ContentReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports")
    public ResponseEntity<ReportResponseDto> createReport(@Valid @RequestBody ReportRequestDto request) {
        return new ResponseEntity<>(reportService.createReport(request), HttpStatus.CREATED);
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<List<ReportResponseDto>> listReports(@RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.listReports(status));
    }

    @GetMapping("/admin/reports/{id}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }

    @PutMapping("/admin/reports/{id}/review")
    public ResponseEntity<ReportResponseDto> reviewReport(@PathVariable Long id,
                                                          @Valid @RequestBody ReportReviewRequestDto request) {
        return ResponseEntity.ok(reportService.reviewReport(id, request));
    }
}
