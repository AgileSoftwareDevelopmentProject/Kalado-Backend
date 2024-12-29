package com.kalado.reporting.domain.model;

import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import com.kalado.common.enums.ReportStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "SUBMITTED")
    @Mapping(target = "reporterId", ignore = true)
    @Mapping(target = "adminNotes", ignore = true)
    Report toReport(ReportCreateRequestDto request);

    @Mapping(target = "status", expression = "java(report.getStatus())")
    ReportResponseDto toReportResponse(Report report);

    List<ReportResponseDto> toReportResponse(List<Report> reports);

    default ReportStatus mapStringToReportStatus(String status) {
        if (status == null) {
            return null;
        }
        return ReportStatus.valueOf(status);
    }
}