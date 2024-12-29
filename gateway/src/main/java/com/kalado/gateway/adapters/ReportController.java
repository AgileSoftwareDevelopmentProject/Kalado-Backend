package com.kalado.gateway.adapters;

import com.kalado.common.dto.*;
import com.kalado.common.feign.report.ReportApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportApi reportApi;

    // User endpoints
    @PostMapping
    @Authentication(userId = "#userId")
    public ReportResponseDto createReport(
            @RequestBody ReportCreateRequestDto request,
            Long userId) {
        return reportApi.createReport(request, userId);
    }

    @GetMapping("/my-reports")
    @Authentication(userId = "#userId")
    public List<ReportResponseDto> getMyReports(Long userId) {
        return reportApi.getUserReports(userId);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @Authentication(userId = "#userId")
    public List<ReportResponseDto> getAllReports(
            @RequestParam(required = false) String status,
            Long adminId) {
        return reportApi.getAllReports(status, adminId);
    }

    @PatchMapping("/admin/{reportId}/status")
    @Authentication(userId = "#userId")
    public ReportResponseDto updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportStatusUpdateDto request,
            Long adminId) {
        return reportApi.updateReportStatus(reportId, request, adminId);
    }

    @GetMapping("/admin/statistics")
    @Authentication(userId = "#userId")
    public ReportStatisticsDto getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String violationType,
            Long adminId) {
        return reportApi.getStatistics(startDate, endDate, violationType, adminId);
    }

    @GetMapping("/admin/statistics/export")
    @Authentication(userId = "#userId")
    public byte[] exportStatistics(
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Long adminId) {
        return reportApi.exportStatistics(format, startDate, endDate, adminId);
    }
}