package com.kalado.reporting.domain.model;

import javax.persistence.*;

import com.kalado.common.enums.ReportStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String violationType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Column(name = "reported_user_id")
    private Long reportedUserId;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @ElementCollection
    @CollectionTable(name = "report_evidence", joinColumns = @JoinColumn(name = "report_id"))
    private List<String> evidenceUrls;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<AdminNote> adminNotes;
}

