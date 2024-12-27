package com.kalado.reporting.domain.model;

import com.kalado.common.dto.AdminNoteDto;
import com.kalado.common.dto.ReportCreateRequestDto;
import com.kalado.common.dto.ReportResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reporterId", ignore = true)
    @Mapping(target = "evidenceUrls", ignore = true)
    @Mapping(target = "adminNotes", ignore = true)
    Report toReport(ReportCreateRequestDto request);

    @Mapping(target = "status", expression = "java(report.getStatus().toString())")
    ReportResponseDto toReportResponse(Report report);

    AdminNoteDto toAdminNoteDto(AdminNote adminNote);
    List<AdminNoteDto> toAdminNoteDtos(List<AdminNote> adminNotes);
}