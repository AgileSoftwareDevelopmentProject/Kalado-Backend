package com.kalado.reporting;

import com.kalado.common.dto.*;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.ProductStatus;
import com.kalado.common.enums.ReportStatus;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import com.kalado.reporting.application.service.*;
import com.kalado.reporting.domain.model.Report;
import com.kalado.reporting.domain.model.ReportMapper;
import com.kalado.reporting.domain.model.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock
    private ReportValidator reportValidator;
    @Mock
    private ReportEvidenceHandler evidenceHandler;
    @Mock
    private ReportFactory reportFactory;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ProductApi productApi;
    @Mock
    private UserApi userApi;
    @Mock
    private EmailService emailService;
    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportStatusUpdater reportStatusUpdater;

    private static final Long REPORT_ID = 1L;
    private static final Long ADMIN_ID = 2L;

    private ReportCreateRequestDto validRequest;
    private static final Long REPORTER_ID = 1L;
    private static final Long REPORTED_CONTENT_ID = 2L;
    private static final Long REPORTED_USER_ID = 3L;

    @BeforeEach
    void setUp() {
        validRequest = ReportCreateRequestDto.builder()
                .violationType("INAPPROPRIATE_CONTENT")
                .description("Test description")
                .reportedContentId(REPORTED_CONTENT_ID)
                .evidenceFiles(Collections.emptyList())
                .build();
    }

    @Test
    void createReport_Success() {
        // Arrange
        ProductDto mockProduct = ProductDto.builder()
                .id(REPORTED_CONTENT_ID)
                .sellerId(REPORTED_USER_ID)
                .build();

        Report expectedReport = Report.builder()
                .id(1L)
                .reporterId(REPORTER_ID)
                .reportedUserId(REPORTED_USER_ID)
                .status(ReportStatus.SUBMITTED)
                .build();

        ReportResponseDto expectedResponse = ReportResponseDto.builder()
                .id(1L)
                .status(ReportStatus.SUBMITTED)
                .build();

        // Mock behaviors
        when(productApi.getProduct(REPORTED_CONTENT_ID)).thenReturn(mockProduct);
        doNothing().when(reportValidator).validateReportCreation(validRequest, REPORTER_ID);
        when(evidenceHandler.processEvidenceFiles(validRequest.getEvidenceFiles()))
                .thenReturn(Collections.emptyList());
        when(reportFactory.createReport(
                eq(validRequest),
                eq(REPORTER_ID),
                eq(REPORTED_USER_ID),
                anyList()
        )).thenReturn(expectedReport);
        when(reportRepository.save(expectedReport)).thenReturn(expectedReport);

        // Act
        ReportResponseDto response = reportService.createReport(validRequest, REPORTER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse.getId(), response.getId());

        // Verify method invocations
        verify(reportValidator).validateReportCreation(validRequest, REPORTER_ID);
        verify(evidenceHandler).processEvidenceFiles(validRequest.getEvidenceFiles());
        verify(reportFactory).createReport(
                eq(validRequest),
                eq(REPORTER_ID),
                eq(REPORTED_USER_ID),
                anyList()
        );
        verify(reportRepository).save(expectedReport);
        verify(emailService).sendReportConfirmation(REPORTER_ID);
    }

    @Test
    void updateReportStatus_Success() {
        // Arrange
        Report existingReport = Report.builder()
                .id(REPORT_ID)
                .status(ReportStatus.SUBMITTED)
                .build();

        ReportStatusUpdateDto updateRequest = ReportStatusUpdateDto.builder()
                .status(ReportStatus.RESOLVED)
                .adminNotes("Test notes")
                .build();

        ReportResponseDto expectedResponse = ReportResponseDto.builder()
                .id(REPORT_ID)
                .status(ReportStatus.RESOLVED)
                .build();

        // Mock behaviors
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(existingReport));
        when(reportStatusUpdater.updateReportStatus(
                eq(existingReport),
                eq(updateRequest),
                eq(ADMIN_ID)
        )).thenReturn(existingReport);
        when(reportMapper.toReportResponse(existingReport)).thenReturn(expectedResponse);

        // Act
        ReportResponseDto response = reportService.updateReportStatus(
                REPORT_ID,
                updateRequest,
                ADMIN_ID
        );

        // Assert
        assertNotNull(response);
        assertEquals(ReportStatus.RESOLVED, response.getStatus());

        // Verify method invocations
        verify(reportRepository).findById(REPORT_ID);
        verify(reportStatusUpdater).updateReportStatus(
                eq(existingReport),
                eq(updateRequest),
                eq(ADMIN_ID)
        );
        verify(reportMapper).toReportResponse(existingReport);
    }

    @Test
    void updateReportStatus_ReportNotFound() {
        // Arrange
        ReportStatusUpdateDto updateRequest = ReportStatusUpdateDto.builder()
                .status(ReportStatus.RESOLVED)
                .build();

        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(
                CustomException.class,
                () -> reportService.updateReportStatus(REPORT_ID, updateRequest, ADMIN_ID)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("Report not found", exception.getMessage());

        verify(reportRepository).findById(REPORT_ID);
        verifyNoInteractions(reportStatusUpdater);
        verifyNoInteractions(reportMapper);
    }

    @Test
    void getUserReports_Success() {
        when(userApi.getUserProfile(REPORTER_ID)).thenReturn(UserDto.builder().id(REPORTER_ID).build());
        when(reportRepository.findByReporterId(REPORTER_ID))
                .thenReturn(Arrays.asList(
                        Report.builder().id(1L).build(),
                        Report.builder().id(2L).build()
                ));
        when(reportMapper.toReportResponse(any())).thenReturn(new ReportResponseDto());

        List<ReportResponseDto> reports = reportService.getUserReports(REPORTER_ID);

        assertNotNull(reports);
        assertEquals(2, reports.size());
        verify(reportMapper, times(2)).toReportResponse(any());
    }

    @Test
    void getUserReports_UserNotFound() {
        when(userApi.getUserProfile(REPORTER_ID)).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class,
                () -> reportService.getUserReports(REPORTER_ID));
        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
    }
}