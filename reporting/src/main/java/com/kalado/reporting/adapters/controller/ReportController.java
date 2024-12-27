package com.kalado.reporting.adapters.controller;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.common.enums.ReportStatus;
import com.kalado.reporting.application.service.ReportService;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ReportMapper reportMapper;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @RequestBody ReportCreateRequestDto request,
            @RequestAttribute("userId") Long reporterId) {
        Report report = reportService.createReport(request, null, reporterId);
        return ResponseEntity.ok(reportMapper.toReportResponse(report));
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ReportResponseDto> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportStatusUpdateDto request,
            @RequestAttribute("userId") Long adminId) {
        Report report = reportService.updateReportStatus(reportId, request, adminId);
        return ResponseEntity.ok(reportMapper.toReportResponse(report));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getAllReports(
            @RequestParam(required = false) ReportStatus status) {
        List<Report> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status);
        } else {
            reports = reportService.getAllReports();
        }
        return ResponseEntity.ok(reports.stream()
                .map(reportMapper::toReportResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportResponseDto>> getMyReports(
            @RequestAttribute("userId") Long reporterId) {
        List<Report> reports = reportService.getReportsByReporter(reporterId);
        return ResponseEntity.ok(reports.stream()
                .map(reportMapper::toReportResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long reportId) {
        Report report = reportService.getReport(reportId);
        return ResponseEntity.ok(reportMapper.toReportResponse(report));
    }
}