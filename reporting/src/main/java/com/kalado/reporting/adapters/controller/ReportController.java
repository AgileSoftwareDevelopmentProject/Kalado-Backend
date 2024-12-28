package com.kalado.reporting.adapters.controller;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.common.enums.ReportStatus;
import com.kalado.common.feign.report.ReportApi;
import com.kalado.reporting.application.service.ReportService;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController implements ReportApi {
    private final ReportService reportService;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponseDto createReport(
            @RequestBody ReportCreateRequestDto request,
            @RequestParam("userId") Long reporterId) {
        Report report = reportService.createReport(request, null, reporterId);
        return reportMapper.toReportResponse(report);
    }

    @Override
    public ReportResponseDto updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportStatusUpdateDto request,
            @RequestParam("userId") Long adminId) {
        Report report = reportService.updateReportStatus(reportId, request, adminId);
        return reportMapper.toReportResponse(report);
    }

    @Override
    public List<ReportResponseDto> getReportsByUser(Long userId) {
        return List.of();
    }

    @Override
    public List<ReportResponseDto> getAllReports(
            @RequestParam(required = false) ReportStatus status) {
        List<Report> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status);
        } else {
            reports = reportService.getAllReports();
        }
        return reports.stream()
                .map(reportMapper::toReportResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportResponseDto> getMyReports(
            @RequestParam("userId") Long reporterId) {
        List<Report> reports = reportService.getReportsByReporter(reporterId);
        return reports.stream()
                .map(reportMapper::toReportResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReportResponseDto getReport(@PathVariable Long reportId) {
        Report report = reportService.getReport(reportId);
        return reportMapper.toReportResponse(report);
    }
}