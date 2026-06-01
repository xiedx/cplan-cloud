package com.cplan.storage.mq;

import com.cplan.common.constant.MqTopicConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer for potential file storage task messages.
 * Currently a placeholder — file uploads are handled via presigned URLs.
 * Reserved for future async storage operations (e.g., temp file cleanup, batch operations).
 */
@Component
@RocketMQMessageListener(
        topic = "CPLAN_STORAGE_TASK",
        consumerGroup = "CPLAN_STORAGE_TASK-CONSUMER"
)
public class StorageTaskConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(StorageTaskConsumer.class);

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Received storage task message: {}", message);
        // Placeholder for future storage-related async tasks
        // e.g., auto-cleanup of temp files, batch file operations
    }
}
