package com.kalado.reporting.application.service;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.ReportStatus;
import com.kalado.common.exception.CustomException;
import com.kalado.reporting.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
//    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @Transactional
    public Report createReport(ReportCreateRequestDto request, List<MultipartFile> evidence, Long reporterId) {
//        List<String> evidenceUrls = new ArrayList<>();
//
//        if (evidence != null && !evidence.isEmpty()) {
//            evidenceUrls = evidence.stream()
//                    .map(fileStorageService::storeFile)
//                    .collect(Collectors.toList());
//        }

        Report report = Report.builder()
                .violationType(request.getViolationType())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .reporterId(reporterId)
                .reportedUserId(request.getReportedUserId())
                .status(ReportStatus.SUBMITTED)
//                .evidenceUrls(evidenceUrls)
                .adminNotes(new ArrayList<>())
                .build();

        Report savedReport = reportRepository.save(report);
        emailService.sendReportConfirmation(reporterId);
        return savedReport;
    }

    @Transactional
    public Report updateReportStatus(Long reportId, ReportStatusUpdateDto request, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Report not found"));

        ReportStatus newStatus = ReportStatus.valueOf(request.getStatus());
        report.setStatus(newStatus);

        if (request.getAdminNote() != null && !request.getAdminNote().isEmpty()) {
            AdminNote note = AdminNote.builder()
                    .content(request.getAdminNote())
                    .adminId(adminId)
                    .createdAt(LocalDateTime.now())
                    .report(report)
                    .build();
            report.getAdminNotes().add(note);
        }

        Report updatedReport = reportRepository.save(report);
        emailService.sendReportStatusUpdate(report.getReporterId(), report.getStatus());
        return updatedReport;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getReportsByReporter(Long reporterId) {
        return reportRepository.findByReporterId(reporterId);
    }

    public Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Report not found"));
    }

    public List<Report> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status);
    }
}