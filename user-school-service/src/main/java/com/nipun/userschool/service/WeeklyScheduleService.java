package com.nipun.userschool.service;

import com.nipun.userschool.entity.WeeklySchedule;
import com.nipun.userschool.repository.WeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public WeeklySchedule saveSchedule(WeeklySchedule schedule) {
        log.info("Saving weekly schedule for teacher: {}, subject: {}, day: {}",
                schedule.getTeacherId(), schedule.getSubjectId(), schedule.getDayOfWeek());
        return weeklyScheduleRepository.save(schedule);
    }

    public List<WeeklySchedule> getAllSchedules() {
        return weeklyScheduleRepository.findAll();
    }

    public WeeklySchedule getScheduleById(UUID id) {
        return weeklyScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weekly schedule not found with id: " + id));
    }

    public List<WeeklySchedule> getSchedulesByTeacher(UUID teacherId) {
        return weeklyScheduleRepository.findByTeacherId(teacherId);
    }

    public List<WeeklySchedule> getSchedulesByDay(String dayOfWeek) {
        return weeklyScheduleRepository.findByDayOfWeekIgnoreCase(dayOfWeek);
    }

    public List<WeeklySchedule> getSchedulesByTeacherAndDay(UUID teacherId, String dayOfWeek) {
        return weeklyScheduleRepository.findByTeacherIdAndDayOfWeekIgnoreCase(teacherId, dayOfWeek);
    }

    public WeeklySchedule updateSchedule(UUID id, WeeklySchedule updatedSchedule) {
        WeeklySchedule existing = getScheduleById(id);

        existing.setTeacherId(updatedSchedule.getTeacherId());
        existing.setSubjectId(updatedSchedule.getSubjectId());
        existing.setLessonPlanId(updatedSchedule.getLessonPlanId());
        existing.setWeekNumber(updatedSchedule.getWeekNumber());
        existing.setDayOfWeek(updatedSchedule.getDayOfWeek());
        existing.setReminderTime(updatedSchedule.getReminderTime());
        existing.setActive(updatedSchedule.getActive());

        return weeklyScheduleRepository.save(existing);
    }

    public void deleteSchedule(UUID id) {
        WeeklySchedule existing = getScheduleById(id);
        weeklyScheduleRepository.delete(existing);
    }
}