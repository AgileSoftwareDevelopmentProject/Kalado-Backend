package com.kalado.reporting.application.service;

import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.ReportStatus;
import com.kalado.common.exception.CustomException;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
  private final ReportRepository reportRepository;
  private final UserApi userApi;
  private final ReportMapper reportMapper;

  @Transactional
  public ReportResponseDto createReport(ReportCreateRequestDto request, Long reporterId) {
    validateRequest(request);
    validateUser(reporterId);
    validateUser(request.getReportedUserId());

    Report report =
        Report.builder()
            .violationType(request.getViolationType())
            .description(request.getDescription())
            .reporterId(reporterId)
            .reportedUserId(request.getReportedUserId())
            .reportedContentId(request.getReportedContentId())
            .status(ReportStatus.SUBMITTED)
            .createdAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .build();

    Report savedReport = reportRepository.save(report);
    log.info("Created report with ID: {} by user: {}", savedReport.getId(), reporterId);

    return reportMapper.toReportResponse(savedReport);
  }

  @Transactional
  public ReportResponseDto updateReportStatus(
      Long reportId, ReportStatusUpdateDto request, Long adminId) {
    validateUser(adminId);
    Report report = getReportById(reportId);

    report.setStatus(request.getStatus());
    report.setAdminId(adminId);
    report.setLastUpdatedAt(LocalDateTime.now());
    report.setAdminNotes(request.getAdminNotes());

    if (request.isBlockUser()) {
      try {
        userApi.blockUser(report.getReportedUserId());
        report.setUserBlocked(true);
      } catch (Exception e) {
        log.error("Failed to block user: {}", report.getReportedUserId(), e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to block user");
      }
    }

    Report updatedReport = reportRepository.save(report);
    return reportMapper.toReportResponse(updatedReport);
  }

  private Report getReportById(Long reportId) {
    return reportRepository
        .findById(reportId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Report not found"));
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

  private void validateRequest(ReportCreateRequestDto request) {
    if (request == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Request cannot be null");
    }
    if (request.getViolationType() == null || request.getViolationType().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Violation type is required");
    }
    if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Description is required");
    }
    if (request.getReportedUserId() == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Reported user ID is required");
    }
  }

  @Transactional(readOnly = true)
  public ReportStatisticsDto getStatistics(
      LocalDateTime startDate, LocalDateTime endDate, String violationType) {
    List<Report> reports =
        reportRepository.findByDateRangeAndType(startDate, endDate, violationType);

    long totalReports = reports.size();
    long pendingReports =
        reports.stream().filter(report -> report.getStatus() == ReportStatus.SUBMITTED).count();
    long resolvedReports =
        reports.stream().filter(report -> report.getStatus() == ReportStatus.RESOLVED).count();
    long rejectedReports =
        reports.stream().filter(report -> report.getStatus() == ReportStatus.REJECTED).count();

    Map<String, Long> reportsByType =
        reports.stream()
            .collect(Collectors.groupingBy(Report::getViolationType, Collectors.counting()));

    Map<String, Long> reportsByStatus =
        reports.stream()
            .collect(
                Collectors.groupingBy(
                    report -> report.getStatus().toString(), Collectors.counting()));

    return ReportStatisticsDto.builder()
        .totalReports(totalReports)
        .pendingReports(pendingReports)
        .resolvedReports(resolvedReports)
        .rejectedReports(rejectedReports)
        .reportsByType(reportsByType)
        .reportsByStatus(reportsByStatus)
        .build();
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
}
