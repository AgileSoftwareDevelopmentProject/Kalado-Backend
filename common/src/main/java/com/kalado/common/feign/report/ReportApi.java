package com.kalado.common.feign.report;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.common.dto.ReportStatisticsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "reporting-service", path = "/reports")
public interface ReportApi {
    @PostMapping
    ReportResponseDto createReport(
            @RequestBody ReportCreateRequestDto request,
            @RequestParam("userId") Long reporterId
    );

    @GetMapping("/user/{userId}")
    List<ReportResponseDto> getUserReports(
            @PathVariable Long userId
    );

    // Admin endpoints
    @GetMapping("/admin/all")
    List<ReportResponseDto> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam("adminId") Long adminId
    );

    @PatchMapping("/admin/{reportId}/status")
    ReportResponseDto updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody ReportStatusUpdateDto request,
            @RequestParam("adminId") Long adminId
    );

    @GetMapping("/admin/statistics")
    ReportStatisticsDto getStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String violationType,
            @RequestParam("adminId") Long adminId
    );

    @GetMapping("/admin/statistics/export")
    byte[] exportStatistics(
            @RequestParam String format,  // "PDF", "CSV", or "EXCEL"
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam("adminId") Long adminId
    );
}