package com.kalado.reporting.application.service;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.enums.ReportStatus;
import com.kalado.reporting.domain.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportFactory {
    public Report createReport(
            ReportCreateRequestDto request,
            Long reporterId,
            Long reportedUserId,
            List<String> evidenceUrls
    ) {
        return Report.builder()
                .violationType(request.getViolationType())
                .description(request.getDescription())
                .reporterId(reporterId)
                .reportedUserId(reportedUserId)
                .reportedContentId(request.getReportedContentId())
                .evidenceFiles(evidenceUrls)
                .status(ReportStatus.SUBMITTED)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }
}
