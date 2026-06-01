package com.cplan.ai.mq;

import com.cplan.ai.service.VideoGenService;
import com.cplan.common.constant.MqTopicConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer for CPLAN_VIDEO_GEN messages.
 * Calls the video generation service, then publishes
 * CPLAN_VIDEO_GEN_DONE with the result.
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_VIDEO_GEN,
        consumerGroup = MqTopicConstant.GROUP_VIDEO_GEN
)
public class VideoGenConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(VideoGenConsumer.class);

    private final VideoGenService videoGenService;
    private final RocketMQTemplate rocketMQTemplate;

    public VideoGenConsumer(VideoGenService videoGenService, RocketMQTemplate rocketMQTemplate) {
        this.videoGenService = videoGenService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Consuming VIDEO_GEN message: {}", message);

        Long projectId = toLong(message.get("projectId"));
        Long storyboardId = toLong(message.get("storyboardId"));
        Long videoTaskId = toLong(message.get("videoTaskId"));
        String videoPrompt = (String) message.getOrDefault("videoPrompt", "");
        int durationSeconds = 5;
        if (message.get("durationSeconds") instanceof Number num) {
            durationSeconds = num.intValue();
        }

        try {
            // Call video generation service
            String videoUrl = videoGenService.generateVideo(videoPrompt, durationSeconds);

            // Publish VIDEO_GEN_DONE
            Map<String, Object> doneMessage = new HashMap<>();
            doneMessage.put("projectId", projectId);
            doneMessage.put("storyboardId", storyboardId);
            doneMessage.put("videoTaskId", videoTaskId);
            doneMessage.put("status", "SUCCESS");
            doneMessage.put("videoUrl", videoUrl);

            Message<Map<String, Object>> mqMessage = MessageBuilder.withPayload(doneMessage).build();
            rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_VIDEO_GEN_DONE, mqMessage);

            log.info("VIDEO_GEN_DONE published: projectId={}, storyboardId={}", projectId, storyboardId);

        } catch (Exception e) {
            log.error("Video generation failed: projectId={}, storyboardId={}, error={}",
                    projectId, storyboardId, e.getMessage());

            // Publish failure message
            Map<String, Object> failMessage = new HashMap<>();
            failMessage.put("projectId", projectId);
            failMessage.put("storyboardId", storyboardId);
            failMessage.put("videoTaskId", videoTaskId);
            failMessage.put("status", "FAILED");
            failMessage.put("errorMessage", e.getMessage());

            Message<Map<String, Object>> mqMessage = MessageBuilder.withPayload(failMessage).build();
            rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_VIDEO_GEN_DONE, mqMessage);
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
