package com.nipun.curriculum.repository;

import com.nipun.curriculum.entity.LessonPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonPlanRepository extends JpaRepository<LessonPlan, UUID> {
    List<LessonPlan> findBySubjectId(UUID subjectId);
}
