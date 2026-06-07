package com.nipun.userschool.repository;

import com.nipun.userschool.entity.WeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, UUID> {

    List<WeeklySchedule> findByTeacherId(UUID teacherId);

    List<WeeklySchedule> findByDayOfWeekIgnoreCase(String dayOfWeek);

    List<WeeklySchedule> findByTeacherIdAndDayOfWeekIgnoreCase(UUID teacherId, String dayOfWeek);
}