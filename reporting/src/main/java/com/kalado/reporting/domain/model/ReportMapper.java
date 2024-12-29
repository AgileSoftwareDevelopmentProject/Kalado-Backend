package com.kalado.reporting.domain.model;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.enums.ReportStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReportMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastUpdatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "SUBMITTED")
    @Mapping(target = "adminId", ignore = true)
    @Mapping(target = "userBlocked", constant = "false")
    Report toReport(ReportCreateRequestDto request);

    ReportResponseDto toReportResponse(Report report);

    List<ReportResponseDto> toReportResponseList(List<Report> reports);

    default ReportStatus mapStatusString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}