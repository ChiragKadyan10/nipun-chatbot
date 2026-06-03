package com.nipun.scheduling.service;

import com.nipun.scheduling.entity.WeeklySchedule;
import com.nipun.scheduling.job.WeeklyReminderJob;
import com.nipun.scheduling.repository.WeeklyScheduleRepository;
import com.nipun.shared.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final Scheduler scheduler;
    private final WeeklyScheduleRepository weeklyScheduleRepository;

    @Transactional
    public WeeklySchedule saveAndSchedule(WeeklySchedule schedule, String teacherPhone, String teacherName, String lessonPlanTitle) {
        String tenantId = TenantContext.getTenantId();
        log.info("Saving schedule and registering Quartz job for tenant: {}", tenantId);
        
        WeeklySchedule savedSchedule = weeklyScheduleRepository.save(schedule);
        
        // Define Quartz Job
        JobDetail jobDetail = JobBuilder.newJob(WeeklyReminderJob.class)
                .withIdentity("job_" + savedSchedule.getId(), "group_" + tenantId)
                .usingJobData("teacherPhone", teacherPhone)
                .usingJobData("teacherName", teacherName)
                .usingJobData("tenantId", tenantId)
                .usingJobData("lessonPlanTitle", lessonPlanTitle)
                .usingJobData("weekNumber", schedule.getWeekNumber())
                .build();

        // Convert Schedule to Cron Expression (e.g. Monday at 08:30 -> "0 30 8 ? * MON")
        String cronExpression = buildCronExpression(schedule.getDayOfWeek(), schedule.getReminderTime());

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + savedSchedule.getId(), "group_" + tenantId)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Successfully scheduled Quartz Job with trigger key: {} under cron: {}", trigger.getKey(), cronExpression);
        } catch (SchedulerException e) {
            log.error("Failed to schedule Quartz Job", e);
            throw new RuntimeException("Quartz scheduling failed", e);
        }

        return savedSchedule;
    }

    public void pauseSchedule(UUID scheduleId) {
        String tenantId = TenantContext.getTenantId();
        log.info("Pausing Quartz trigger: {}", scheduleId);
        try {
            scheduler.pauseTrigger(new TriggerKey("trigger_" + scheduleId, "group_" + tenantId));
        } catch (SchedulerException e) {
            log.error("Failed to pause schedule trigger", e);
        }
    }

    public void resumeSchedule(UUID scheduleId) {
        String tenantId = TenantContext.getTenantId();
        log.info("Resuming Quartz trigger: {}", scheduleId);
        try {
            scheduler.resumeTrigger(new TriggerKey("trigger_" + scheduleId, "group_" + tenantId));
        } catch (SchedulerException e) {
            log.error("Failed to resume schedule trigger", e);
        }
    }

    private String buildCronExpression(String dayOfWeek, LocalTime time) {
        // Map common day codes
        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("MONDAY", "MON");
        dayMap.put("TUESDAY", "TUE");
        dayMap.put("WEDNESDAY", "WED");
        dayMap.put("THURSDAY", "THU");
        dayMap.put("FRIDAY", "FRI");
        dayMap.put("SATURDAY", "SAT");
        dayMap.put("SUNDAY", "SUN");

        String shortDay = dayMap.getOrDefault(dayOfWeek.toUpperCase(), "MON");
        return String.format("0 %d %d ? * %s", time.getMinute(), time.getHour(), shortDay);
    }
}
