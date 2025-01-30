package com.kalado.reporting.application.service;

import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportMapper;
import com.kalado.reporting.domain.model.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportValidator reportValidator;
    private final ReportEvidenceHandler evidenceHandler;
    private final ReportFactory reportFactory;
    private final ReportRepository reportRepository;
    private final UserApi userApi;
    private final EmailService emailService;
    private final ReportMapper reportMapper;
    private final ProductApi productApi;
    private final ReportStatusUpdater reportStatusUpdater;
    private final ReportStatisticsCalculator statisticsCalculator;

    @Transactional
    public ReportResponseDto createReport(
            ReportCreateRequestDto request,
            Long reporterId
    ) {
        reportValidator.validateReportCreation(request, reporterId);

        Long reportedUserId = findReportedUserId(request.getReportedContentId());

        validateNotSelfReporting(reporterId, reportedUserId);

        List<String> evidenceUrls = evidenceHandler.processEvidenceFiles(
                request.getEvidenceFiles()
        );

        Report report = reportFactory.createReport(
                request,
                reporterId,
                reportedUserId,
                evidenceUrls
        );

        Report savedReport = reportRepository.save(report);

        sendReportConfirmationEmail(reporterId);

        return convertToResponseDto(savedReport);
    }

    @Transactional
    public ReportResponseDto updateReportStatus(
            Long reportId,
            ReportStatusUpdateDto request,
            Long adminId
    ) {
        Report report = findReportById(reportId);

        Report updatedReport = reportStatusUpdater.updateReportStatus(
                report, request, adminId
        );

        return reportMapper.toReportResponse(updatedReport);
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getUserReports(Long userId) {
        validateUser(userId);
        List<Report> reports = reportRepository.findByReporterId(userId);
        return reports.stream().map(reportMapper::toReportResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream().map(reportMapper::toReportResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportStatisticsDto getStatistics(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String violationType
    ) {
        return statisticsCalculator.calculateStatistics(
                startDate,
                endDate,
                violationType
        );
    }

    private Report findReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.NOT_FOUND,
                        "Report not found"
                ));
    }


    private Long findReportedUserId(Long productId) {
        try {
            ProductDto product = productApi.getProduct(productId);
            return product.getSellerId();
        } catch (Exception e) {
            log.error("Failed to find reported user for product: {}", productId, e);
            throw new CustomException(
                    ErrorCode.NOT_FOUND,
                    "Unable to find product owner"
            );
        }
    }

    private void validateNotSelfReporting(Long reporterId, Long reportedUserId) {
        if (reporterId.equals(reportedUserId)) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "Cannot report your own product"
            );
        }
    }

    private void sendReportConfirmationEmail(Long reporterId) {
        try {
            emailService.sendReportConfirmation(reporterId);
        } catch (Exception e) {
            log.warn("Failed to send report confirmation email", e);
        }
    }

    private ReportResponseDto convertToResponseDto(Report report) {
        return ReportResponseDto.builder()
                .id(report.getId())
                .violationType(report.getViolationType())
                .description(report.getDescription())
                .reporterId(report.getReporterId())
                .reportedContentId(report.getReportedContentId())
                .evidenceFiles(report.getEvidenceFiles())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private void validateUser(Long userId) {
        try {
            UserDto user = userApi.getUserProfile(userId);
            if (user == null) {
                throw new CustomException(ErrorCode.NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            log.error("Error validating user: {}", userId, e);
            throw new CustomException(ErrorCode.NOT_FOUND, "User not found");
        }
    }
}
