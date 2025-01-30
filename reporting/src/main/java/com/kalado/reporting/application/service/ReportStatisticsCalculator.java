package com.kalado.reporting.application.service;

import com.kalado.common.dto.ReportStatisticsDto;
import com.kalado.common.enums.ReportStatus;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportStatisticsCalculator {
    private final ReportRepository reportRepository;

    public ReportStatisticsDto calculateStatistics(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String violationType
    ) {
        List<Report> reports = fetchReports(startDate, endDate, violationType);

        return ReportStatisticsDto.builder()
                .totalReports(calculateTotalReports(reports))
                .pendingReports(countReportsByStatus(reports, ReportStatus.SUBMITTED))
                .resolvedReports(countReportsByStatus(reports, ReportStatus.RESOLVED))
                .rejectedReports(countReportsByStatus(reports, ReportStatus.REJECTED))
                .averageResolutionTimeInHours(calculateAverageResolutionTime(reports))
                .reportsByType(groupReportsByType(reports))
                .reportsByStatus(groupReportsByStatus(reports))
                .build();
    }

    private List<Report> fetchReports(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String violationType
    ) {
        if (startDate != null && endDate != null) {
            if (violationType != null && !violationType.trim().isEmpty()) {
                return reportRepository
                        .findByCreatedAtBetweenAndViolationTypeContainingIgnoreCase(
                                startDate, endDate, violationType
                        );
            }
            return reportRepository.findByCreatedAtBetween(startDate, endDate);
        }
        return reportRepository.findAll();
    }

    private long calculateTotalReports(List<Report> reports) {
        return reports.size();
    }

    private long countReportsByStatus(List<Report> reports, ReportStatus status) {
        return reports.stream()
                .filter(report -> report.getStatus() == status)
                .count();
    }

    private double calculateAverageResolutionTime(List<Report> reports) {
        return reports.stream()
                .filter(report -> report.getStatus() == ReportStatus.RESOLVED)
                .mapToDouble(report -> {
                    Duration duration = Duration.between(
                            report.getCreatedAt(),
                            report.getLastUpdatedAt()
                    );
                    return duration.toHours();
                })
                .average()
                .orElse(0.0);
    }

    private Map<String, Long> groupReportsByType(List<Report> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        Report::getViolationType,
                        Collectors.counting()
                ));
    }

    private Map<String, Long> groupReportsByStatus(List<Report> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().toString(),
                        Collectors.counting()
                ));
    }
}