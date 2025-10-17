package com.b1a4.cafeOn.report.repository;

import com.b1a4.cafeOn.report.entity.ReportEntity;
import com.b1a4.cafeOn.report.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    boolean existsByReporterIdAndTargetTypeAndTargetId(String reporterId, TargetType type, Long targetId);
}
