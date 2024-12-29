package com.kalado.reporting.domain.model;

import com.kalado.common.enums.ReportStatus;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String violationType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private Long reportedUserId;

    private Long reportedContentId;

    @Column(name = "status", length = 32, columnDefinition = "varchar(32) default 'NOT_PAID' ")
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private String adminNotes;

    private Long adminId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean userBlocked = false;
}