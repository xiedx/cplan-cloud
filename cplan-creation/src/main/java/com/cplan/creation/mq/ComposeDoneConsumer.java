package com.cplan.creation.mq;

import com.cplan.common.constant.MqTopicConstant;
import com.cplan.creation.entity.Project;
import com.cplan.creation.entity.VideoTask;
import com.cplan.creation.mapper.ProjectMapper;
import com.cplan.creation.mapper.VideoTaskMapper;
import com.cplan.creation.statemachine.TaskEvent;
import com.cplan.creation.statemachine.VideoTaskStateMachine;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consumer that handles compose-done callback messages.
 * Updates the project with the final video URL.
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_COMPOSE_DONE,
        consumerGroup = MqTopicConstant.GROUP_COMPOSE_DONE
)
public class ComposeDoneConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ComposeDoneConsumer.class);

    private final ProjectMapper projectMapper;
    private final VideoTaskMapper videoTaskMapper;
    private final VideoTaskStateMachine stateMachine;
    private final VideoTaskProducer videoTaskProducer;

    public ComposeDoneConsumer(ProjectMapper projectMapper,
                               VideoTaskMapper videoTaskMapper,
                               VideoTaskStateMachine stateMachine,
                               VideoTaskProducer videoTaskProducer) {
        this.projectMapper = projectMapper;
        this.videoTaskMapper = videoTaskMapper;
        this.stateMachine = stateMachine;
        this.videoTaskProducer = videoTaskProducer;
    }

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Received COMPOSE_DONE: {}", message);

        Long projectId = toLong(message.get("projectId"));
        String status = (String) message.getOrDefault("status", "SUCCESS");
        String finalVideoUrl = (String) message.get("finalVideoUrl");
        String errorMessage = (String) message.get("errorMessage");

        if (projectId == null) {
            log.warn("COMPOSE_DONE message missing projectId");
            return;
        }

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            log.warn("Project not found: id={}", projectId);
            return;
        }

        if ("SUCCESS".equals(status)) {
            project.setStatus(2); // COMPLETED
            project.setFinalVideoUrl(finalVideoUrl);
            projectMapper.updateById(project);

            // Notify project completion
            videoTaskProducer.sendNotify(MqTopicConstant.TAG_PROJECT_COMPLETE,
                    Map.of("projectId", projectId,
                            "finalVideoUrl", finalVideoUrl != null ? finalVideoUrl : ""));

            log.info("Project {} completed with final video URL: {}", projectId, finalVideoUrl);
        } else {
            project.setStatus(3); // FAILED
            projectMapper.updateById(project);

            videoTaskProducer.sendNotify(MqTopicConstant.TAG_TASK_PROGRESS,
                    Map.of("projectId", projectId,
                            "type", "COMPOSE",
                            "status", "FAILED",
                            "errorMessage", errorMessage != null ? errorMessage : ""));

            log.warn("Project {} compose failed: {}", projectId, errorMessage);
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
