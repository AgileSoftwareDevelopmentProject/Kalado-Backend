package com.kalado.reporting.application.service;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportStatusUpdater {
    private final ReportRepository reportRepository;
    private final ReportUserHandler userHandler;
    private final ReportProductHandler productHandler;

    @Transactional
    public Report updateReportStatus(
            Report report,
            ReportStatusUpdateDto request,
            Long adminId
    ) {
        updateReportMetadata(report, request, adminId);

        if (request.isBlockUser()) {
            handleUserBlocking(report);
        }

        if (request.isBlockProduct() && report.getReportedContentId() != null) {
            handleProductBlocking(report);
        }

        return reportRepository.save(report);
    }

    private void updateReportMetadata(
            Report report,
            ReportStatusUpdateDto request,
            Long adminId
    ) {
        report.setStatus(request.getStatus());
        report.setAdminId(adminId);
        report.setLastUpdatedAt(LocalDateTime.now());
        report.setAdminNotes(request.getAdminNotes());
    }

    private void handleUserBlocking(Report report) {
        // Block the user
        userHandler.blockUser(report.getReportedUserId());
        report.setUserBlocked(true);

        // Block user's products
        List<ProductDto> userProducts = userHandler.getUserProducts(report.getReportedUserId());
        productHandler.blockMultipleProducts(userProducts);
    }

    private void handleProductBlocking(Report report) {
        productHandler.blockProduct(
                report.getReportedContentId(),
                report.getReportedUserId()
        );
    }
}