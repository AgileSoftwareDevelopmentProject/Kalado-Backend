package com.kalado.reporting.adapters.controller;

import com.kalado.common.dto.*;
import com.kalado.common.feign.report.ReportApi;
import com.kalado.reporting.application.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController implements ReportApi {
    private final ReportService reportService;

    @Override
    public ReportResponseDto createReport(ReportCreateRequestDto request, Long reporterId) {
        return reportService.createReport(request, reporterId);
    }

    @Override
    public List<ReportResponseDto> getUserReports(Long userId) {
        return reportService.getUserReports(userId);
    }

    @Override
    public List<ReportResponseDto> getAllReports(String status, Long adminId) {
        return reportService.getAllReports();
    }

    @Override
    public ReportResponseDto updateReportStatus(Long reportId, ReportStatusUpdateDto request, Long adminId) {
        return reportService.updateReportStatus(reportId, request, adminId);
    }

    @Override
    public ReportStatisticsDto getStatistics(LocalDateTime startDate, LocalDateTime endDate, String violationType, Long adminId) {
//        return reportService.getStatistics(startDate, endDate, violationType, adminId);
    return new ReportStatisticsDto();
    }

    @Override
    public byte[] exportStatistics(String format, LocalDateTime startDate, LocalDateTime endDate, Long adminId) {
//        return reportService.exportStatistics(format, startDate, endDate, adminId);
    return null;
    }
}