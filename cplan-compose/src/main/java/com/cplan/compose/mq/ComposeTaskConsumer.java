package com.cplan.compose.mq;

import com.cplan.common.constant.MqTopicConstant;
import com.cplan.compose.service.VideoComposeService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consumer for CPLAN_COMPOSE_TASK messages.
 * Concatenates all video segments for a project and publishes CPLAN_COMPOSE_DONE.
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_COMPOSE_TASK,
        consumerGroup = MqTopicConstant.GROUP_COMPOSE_TASK
)
public class ComposeTaskConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ComposeTaskConsumer.class);

    private final VideoComposeService videoComposeService;
    private final RocketMQTemplate rocketMQTemplate;

    public ComposeTaskConsumer(VideoComposeService videoComposeService,
                               RocketMQTemplate rocketMQTemplate) {
        this.videoComposeService = videoComposeService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Consuming COMPOSE_TASK message: {}", message);

        Long projectId = toLong(message.get("projectId"));

        if (projectId == null) {
            log.warn("COMPOSE_TASK message missing projectId");
            return;
        }

        try {
            // Collect video segment URLs from the message or fetch from storage
            // In the actual flow, the message should contain the list of video URLs
            @SuppressWarnings("unchecked")
            List<String> videoUrls = (List<String>) message.get("videoUrls");
            if (videoUrls == null) {
                videoUrls = new ArrayList<>();
            }

            // If no URLs provided, use empty list (mock mode handles this)
            String finalVideoUrl = videoComposeService.composeVideos(projectId, videoUrls);

            // Publish COMPOSE_DONE
            Map<String, Object> doneMessage = new HashMap<>();
            doneMessage.put("projectId", projectId);
            doneMessage.put("status", "SUCCESS");
            doneMessage.put("finalVideoUrl", finalVideoUrl);

            Message<Map<String, Object>> mqMessage = MessageBuilder.withPayload(doneMessage).build();
            rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_COMPOSE_DONE, mqMessage);

            log.info("COMPOSE_DONE published: projectId={}, finalUrl={}", projectId, finalVideoUrl);

        } catch (Exception e) {
            log.error("Video composition failed for project {}: {}", projectId, e.getMessage());

            // Publish failure message
            Map<String, Object> failMessage = new HashMap<>();
            failMessage.put("projectId", projectId);
            failMessage.put("status", "FAILED");
            failMessage.put("errorMessage", e.getMessage());

            Message<Map<String, Object>> mqMessage = MessageBuilder.withPayload(failMessage).build();
            rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_COMPOSE_DONE, mqMessage);
        }
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
