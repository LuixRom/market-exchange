package com.dbp.proyectobackendmarketexchange.report.infrastructure;

import com.dbp.proyectobackendmarketexchange.report.domain.ContentReport;
import com.dbp.proyectobackendmarketexchange.report.domain.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {
    List<ContentReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<ContentReport> findAllByOrderByCreatedAtDesc();
}
