package com.kalado.gateway.adapters;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.common.feign.report.ReportApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportApi reportApi;

    @PostMapping
    @Authentication(userId = "#userId")
    public ReportResponseDto createReport(
            @RequestBody ReportCreateRequestDto request,
            Long userId) {
        return reportApi.createReport(request, userId);
    }

    @PatchMapping("/{reportId}/status")
    @Authentication(userId = "#adminId")
    public ReportResponseDto updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportStatusUpdateDto request,
            Long adminId) {
        return reportApi.updateReportStatus(reportId, request, adminId);
    }

    @GetMapping
    @Authentication
    public List<ReportResponseDto> getAllReports(
            @RequestParam(required = false) String status) {
        return reportApi.getAllReports(status);
    }

    @GetMapping("/my-reports")
    @Authentication(userId = "#userId")
    public List<ReportResponseDto> getMyReports(Long userId) {
        return reportApi.getReportsByUser(userId);
    }

    @GetMapping("/{reportId}")
    @Authentication
    public ReportResponseDto getReport(@PathVariable Long reportId) {
        return reportApi.getReport(reportId);
    }
}