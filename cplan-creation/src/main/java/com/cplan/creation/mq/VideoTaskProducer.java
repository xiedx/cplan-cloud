package com.cplan.creation.mq;

import com.cplan.common.constant.MqTopicConstant;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RocketMQ message producer for video-task-related events.
 */
@Component
public class VideoTaskProducer {

    private static final Logger log = LoggerFactory.getLogger(VideoTaskProducer.class);

    private final RocketMQTemplate rocketMQTemplate;

    public VideoTaskProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * Send a notification event (TASK_PROGRESS or PROJECT_COMPLETE).
     */
    public void sendNotify(String tag, Map<String, Object> payload) {
        String destination = MqTopicConstant.TOPIC_NOTIFY + ":" + tag;
        Message<Map<String, Object>> message = MessageBuilder.withPayload(payload).build();
        rocketMQTemplate.syncSend(destination, message);
        log.info("Sent notify: tag={}, payload={}", tag, payload);
    }

    /**
     * Send a compose task event.
     */
    public void sendComposeTask(Map<String, Object> payload) {
        Message<Map<String, Object>> message = MessageBuilder.withPayload(payload).build();
        rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_COMPOSE_TASK, message);
        log.info("Sent compose task: payload={}", payload);
    }
}
