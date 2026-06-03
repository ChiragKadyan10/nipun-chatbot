package com.nipun.scheduling.job;

import com.nipun.shared.context.TenantContext;
import com.nipun.shared.event.WhatsAppMessageSendRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WeeklyReminderJob implements Job {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String OUTGOING_TOPIC = "outgoing-messages";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String teacherPhone = dataMap.getString("teacherPhone");
        String teacherName = dataMap.getString("teacherName");
        String tenantId = dataMap.getString("tenantId");
        String lessonPlanTitle = dataMap.getString("lessonPlanTitle");
        int weekNumber = dataMap.getInt("weekNumber");

        log.info("Executing WeeklyReminderJob for teacher: {} ({}) under tenant: {}", teacherName, teacherPhone, tenantId);

        TenantContext.setTenantId(tenantId);
        try {
            // Formulate personalized curriculum guide guidance message
            String broadcastContent = String.format(
                    "Good morning Teacher %s!\n\n" +
                    "Here is your scheduled teaching guide for Week %d:\n" +
                    "📚 Lesson Plan: *%s*\n\n" +
                    "💡 *Suggested Activity*: Begin the class with a 10-minute brainstorming session to evaluate prerequisite knowledge.\n" +
                    "📝 *Homework Recommendation*: Ask students to complete tasks 1-5 in their workbook.\n\n" +
                    "Reply to this chat if you have any questions or need custom activities for this topic!",
                    teacherName, weekNumber, lessonPlanTitle
            );

            WhatsAppMessageSendRequestEvent sendRequest = WhatsAppMessageSendRequestEvent.builder()
                    .toPhone(teacherPhone)
                    .messageType("TEXT")
                    .content(broadcastContent)
                    .tenantId(tenantId)
                    .build();

            kafkaTemplate.send(OUTGOING_TOPIC, teacherPhone, sendRequest);
            log.info("Dispatched weekly automated schedule reminder to Kafka outbox for phone: {}", teacherPhone);

        } finally {
            TenantContext.clear();
        }
    }
}
