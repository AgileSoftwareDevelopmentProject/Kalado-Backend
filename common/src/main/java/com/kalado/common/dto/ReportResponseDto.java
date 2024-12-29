package com.kalado.common.dto;

import com.kalado.common.enums.ReportStatus;
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
  private Long reporterId;
  private Long reportedUserId;
  private Long reportedContentId;
  private LocalDateTime createdAt;
  private ReportStatus status;
  private String adminNotes;
  private LocalDateTime lastUpdatedAt;
  private Long adminId;
  private boolean userBlocked;
}
