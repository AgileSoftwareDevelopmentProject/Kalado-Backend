package com.kalado.reporting.domain.model;

import com.kalado.common.enums.ReportStatus;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByStatus(ReportStatus status);

    @Query("SELECT r FROM Report r WHERE " +
            "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR r.createdAt <= :endDate) AND " +
            "(:violationType IS NULL OR r.violationType = :violationType)")
    List<Report> findByDateRangeAndType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("violationType") String violationType);
}