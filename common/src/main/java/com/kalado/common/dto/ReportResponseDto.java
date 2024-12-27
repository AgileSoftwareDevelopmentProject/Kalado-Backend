package com.kalado.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Long id;
    private String violationType;
    private String description;
    private LocalDateTime createdAt;
    private Long reporterId;
    private Long reportedUserId;
    private String status;
    private List<AdminNoteDto> adminNotes;
}
