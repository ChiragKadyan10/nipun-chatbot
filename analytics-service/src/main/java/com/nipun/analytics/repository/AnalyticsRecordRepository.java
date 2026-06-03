package com.nipun.analytics.repository;

import com.nipun.analytics.entity.AnalyticsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalyticsRecordRepository extends JpaRepository<AnalyticsRecord, UUID> {
    List<AnalyticsRecord> findByTeacherId(UUID teacherId);
    List<AnalyticsRecord> findBySchoolId(UUID schoolId);
}
