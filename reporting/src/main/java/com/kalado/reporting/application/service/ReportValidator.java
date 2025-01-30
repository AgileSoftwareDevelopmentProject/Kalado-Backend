package com.kalado.reporting.application.service;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.UserDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.common.feign.user.UserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportValidator {
    private final UserApi userApi;
    private final ProductApi productApi;

    public void validateReportCreation(ReportCreateRequestDto request, Long reporterId) {
        validateReporter(reporterId);
        validateReportedContent(request);
        validateReportInput(request);
    }

    private void validateReporter(Long reporterId) {
        try {
            UserDto reporter = userApi.getUserProfile(reporterId);
            if (reporter == null || reporter.isBlocked()) {
                throw new CustomException(
                        ErrorCode.UNAUTHORIZED,
                        "Invalid or blocked reporter"
                );
            }
        } catch (Exception e) {
            log.error("Reporter validation failed for ID: {}", reporterId, e);
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED,
                    "Unable to validate reporter"
            );
        }
    }

    private void validateReportedContent(ReportCreateRequestDto request) {
        try {
            ProductDto product = productApi.getProduct(request.getReportedContentId());
            if (product == null) {
                throw new CustomException(
                        ErrorCode.NOT_FOUND,
                        "Reported product not found"
                );
            }
        } catch (Exception e) {
            log.error("Product validation failed for ID: {}",
                    request.getReportedContentId(), e);
            throw new CustomException(
                    ErrorCode.NOT_FOUND,
                    "Invalid reported content"
            );
        }
    }

    private void validateReportInput(ReportCreateRequestDto request) {
        if (request.getViolationType() == null ||
                request.getViolationType().trim().isEmpty()) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "Violation type is required"
            );
        }

        if (request.getDescription() == null ||
                request.getDescription().trim().isEmpty()) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "Description is required"
            );
        }

        if (request.getReportedContentId() == null) {
            throw new CustomException(
                    ErrorCode.BAD_REQUEST,
                    "Reported content ID is required"
            );
        }
    }
}
