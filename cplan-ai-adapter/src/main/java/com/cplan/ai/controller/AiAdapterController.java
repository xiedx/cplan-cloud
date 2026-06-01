package com.cplan.ai.controller;

import com.cplan.common.constant.MqTopicConstant;
import com.cplan.common.result.Result;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Internal API controller for AI Adapter Service.
 * These endpoints are called by Creation Service via OpenFeign.
 */
@RestController
@RequestMapping("/internal/ai/v1")
public class AiAdapterController {

    private static final Logger log = LoggerFactory.getLogger(AiAdapterController.class);

    private final RocketMQTemplate rocketMQTemplate;

    public AiAdapterController(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * Trigger script generation.
     * POST /internal/ai/v1/script/generate
     */
    @PostMapping("/script/generate")
    public Result<Void> generateScript(@RequestBody Map<String, Object> request) {
        log.info("Received script generation request: projectId={}, outlineId={}",
                request.get("projectId"), request.get("outlineId"));

        // Publish message to RocketMQ for async processing
        Message<Map<String, Object>> message = MessageBuilder.withPayload(request).build();
        rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_SCRIPT_GEN, message);

        log.info("Script gen message published to MQ");
        return Result.ok();
    }

    /**
     * Trigger video generation.
     * POST /internal/ai/v1/video/generate
     */
    @PostMapping("/video/generate")
    public Result<Void> generateVideo(@RequestBody Map<String, Object> request) {
        log.info("Received video generation request: projectId={}, storyboardId={}",
                request.get("projectId"), request.get("storyboardId"));

        // Publish message to RocketMQ for async processing
        Message<Map<String, Object>> message = MessageBuilder.withPayload(request).build();
        rocketMQTemplate.syncSend(MqTopicConstant.TOPIC_VIDEO_GEN, message);

        log.info("Video gen message published to MQ");
        return Result.ok();
    }
}
