package com.roomsetu.repository;

import com.roomsetu.entity.FraudReport;
import com.roomsetu.entity.enums.ReportStatus;
import com.roomsetu.entity.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudReportRepository extends JpaRepository<FraudReport, Long> {
    List<FraudReport> findByStatus(ReportStatus status);
    List<FraudReport> findByTargetTypeAndTargetIdAndStatus(
            ReportTargetType targetType, Long targetId, ReportStatus status);
    long countByTargetTypeAndTargetIdAndStatus(
            ReportTargetType targetType, Long targetId, ReportStatus status);

    boolean existsByReporterIdAndTargetTypeAndTargetIdAndStatus(
            Long reporterId, ReportTargetType targetType, Long targetId, ReportStatus status);
}