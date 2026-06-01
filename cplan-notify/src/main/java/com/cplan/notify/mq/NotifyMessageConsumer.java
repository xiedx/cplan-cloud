package com.cplan.notify.mq;

import com.cplan.common.constant.MqTopicConstant;
import com.cplan.notify.service.SseService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer for CPLAN_NOTIFY messages.
 * Routes notification events to the appropriate SSE connection.
 *
 * <p>Supported tags:
 * <ul>
 *   <li>TASK_PROGRESS — individual task progress updates</li>
 *   <li>PROJECT_COMPLETE — project completion notification</li>
 * </ul>
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_NOTIFY,
        consumerGroup = MqTopicConstant.GROUP_NOTIFY,
        selectorExpression = "TASK_PROGRESS || PROJECT_COMPLETE"
)
public class NotifyMessageConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(NotifyMessageConsumer.class);

    private final SseService sseService;

    public NotifyMessageConsumer(SseService sseService) {
        this.sseService = sseService;
    }

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Consuming NOTIFY message: {}", message);

        Long userId = toLong(message.get("userId"));
        Long projectId = toLong(message.get("projectId"));

        // Determine event type and push to user
        String eventType = determineEventType(message);

        if (userId != null) {
            sseService.pushEvent(userId, eventType, message);
        } else if (projectId != null) {
            // If userId is not in the message, we can't push directly
            // In a real system, we'd look up the project owner
            log.warn("NOTIFY message without userId, projectId={}", projectId);
        }
    }

    private String determineEventType(Map<String, Object> message) {
        if (message.containsKey("finalVideoUrl")) {
            return "project_complete";
        }
        return "task_progress";
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
