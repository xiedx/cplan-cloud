package com.cplan.ai.mq;

import com.cplan.ai.service.LlmService;
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
 * Consumer for CPLAN_SCRIPT_GEN messages.
 * Calls the LLM service to generate a script, then publishes
 * CPLAN_SCRIPT_GEN_DONE with the result.
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_SCRIPT_GEN,
        consumerGroup = MqTopicConstant.GROUP_SCRIPT_GEN
)
public class ScriptGenConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ScriptGenConsumer.class);

    private final LlmService llmService;
    private final RocketMQTemplate rocketMQTemplate;

    public ScriptGenConsumer(LlmService llmService, RocketMQTemplate rocketMQTemplate) {
        this.llmService = llmService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Consuming SCRIPT_GEN message: {}", message);

        Long projectId = toLong(message.get("projectId"));
        Long outlineId = toLong(message.get("outlineId"));
        String outlineContent = (String) message.getOrDefault("outlineContent", "");
        String llmModel = (String) message.get("llmModel");

        try {
            // Call LLM service to generate script
            String scriptContent = llmService.generateScript(outlineContent, llmModel);

            // Parse script content into storyboards (split by "===== 第" delimiter)
            // The script content already contains scene structure from LlmServiceImpl

            // Publish SCRIPT_GEN_DONE with the generated content
            Map<String, Object> doneMessage = new HashMap<>();
            doneMessage.put("projectId", projectId);
            doneMessage.put("outlineId", outlineId);
            doneMessage.put("status", "SUCCESS");
            doneMessage.put("scriptContent", scriptContent);
            doneMessage.put("llmModel", llmModel != null ? llmModel : "deepseek-chat");

            Message<Map<String, Object>> mqMessage = MessageBuilder.withPayload(doneMessage).build();
            rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_SCRIPT_GEN_DONE, mqMessage);

            log.info("SCRIPT_GEN_DONE published: projectId={}, outlineId={}", projectId, outlineId);

        } catch (Exception e) {
            log.error("Script generation failed: projectId={}, error={}", projectId, e.getMessage());

            // Publish failure message
            Map<String, Object> failMessage = new HashMap<>();
            failMessage.put("projectId", projectId);
            failMessage.put("outlineId", outlineId);
            failMessage.put("status", "FAILED");
            failMessage.put("errorMessage", e.getMessage());

            Message<Map<String, Object>> mqMessage = MessageBuilder.withPayload(failMessage).build();
            rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_SCRIPT_GEN_DONE, mqMessage);
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
