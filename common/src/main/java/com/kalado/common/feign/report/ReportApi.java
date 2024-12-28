package com.kalado.common.feign.report;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.common.enums.ReportStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "reporting-service", path = "/reports")
public interface ReportApi {

    @PostMapping
    ReportResponseDto createReport(
            @RequestBody ReportCreateRequestDto request,
            @RequestParam("userId") Long reporterId
    );

    @PatchMapping("/{reportId}/status")
    ReportResponseDto updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportStatusUpdateDto request,
            @RequestParam("userId") Long reporterId
    );

    @GetMapping
    List<ReportResponseDto> getAllReports(
            @RequestParam(required = false) String status
    );

    @GetMapping("/my-reports")
    List<ReportResponseDto> getReportsByUser(
            @RequestParam("userId") Long userId
    );

    List<ReportResponseDto> getAllReports(
            @RequestParam(required = false) ReportStatus status);

    @GetMapping
    List<ReportResponseDto> getMyReports(
            @RequestParam("userId") Long reporterId);

    @GetMapping("/{reportId}")
    ReportResponseDto getReport(
            @PathVariable Long reportId
    );
}