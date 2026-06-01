package com.cplan.creation.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cplan.common.constant.MqTopicConstant;
import com.cplan.creation.entity.*;
import com.cplan.creation.mapper.*;
import com.cplan.creation.statemachine.TaskEvent;
import com.cplan.creation.statemachine.VideoTaskStateMachine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Consumer that handles video-generation-done callback messages.
 * Updates VideoTask and Storyboard statuses, and checks if all storyboards
 * are completed to trigger compose.
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_VIDEO_GEN_DONE,
        consumerGroup = MqTopicConstant.GROUP_VIDEO_GEN_DONE
)
public class VideoGenDoneConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(VideoGenDoneConsumer.class);

    private final VideoTaskMapper videoTaskMapper;
    private final StoryboardMapper storyboardMapper;
    private final ProjectMapper projectMapper;
    private final VideoSegmentMapper videoSegmentMapper;
    private final VideoTaskStateMachine stateMachine;
    private final VideoTaskProducer videoTaskProducer;

    public VideoGenDoneConsumer(VideoTaskMapper videoTaskMapper,
                                StoryboardMapper storyboardMapper,
                                ProjectMapper projectMapper,
                                VideoSegmentMapper videoSegmentMapper,
                                VideoTaskStateMachine stateMachine,
                                VideoTaskProducer videoTaskProducer) {
        this.videoTaskMapper = videoTaskMapper;
        this.storyboardMapper = storyboardMapper;
        this.projectMapper = projectMapper;
        this.videoSegmentMapper = videoSegmentMapper;
        this.stateMachine = stateMachine;
        this.videoTaskProducer = videoTaskProducer;
    }

    @Override
    public void onMessage(Map<String, Object> message) {
        log.info("Received VIDEO_GEN_DONE: {}", message);

        Long videoTaskId = toLong(message.get("videoTaskId"));
        String status = (String) message.getOrDefault("status", "SUCCESS");
        String videoUrl = (String) message.get("videoUrl");
        String errorMessage = (String) message.get("errorMessage");

        if (videoTaskId == null) {
            log.warn("VIDEO_GEN_DONE message missing videoTaskId");
            return;
        }

        VideoTask task = videoTaskMapper.selectById(videoTaskId);
        if (task == null) {
            log.warn("VideoTask not found: id={}", videoTaskId);
            return;
        }

        if ("SUCCESS".equals(status)) {
            stateMachine.transition(task, TaskEvent.SUCCESS);
            task.setFinishedAt(java.time.LocalDateTime.now());
            videoTaskMapper.updateById(task);

            // Update storyboard
            if (task.getStoryboardId() != null) {
                Storyboard storyboard = storyboardMapper.selectById(task.getStoryboardId());
                if (storyboard != null) {
                    storyboard.setStatus(3); // VIDEO_COMPLETED
                    storyboard.setVideoUrl(videoUrl);
                    storyboardMapper.updateById(storyboard);
                }

                // Record video segment
                VideoSegment segment = VideoSegment.builder()
                        .projectId(task.getProjectId())
                        .storyboardId(task.getStoryboardId())
                        .fileKey(videoUrl)
                        .fileUrl(videoUrl)
                        .status(1) // AVAILABLE
                        .build();
                videoSegmentMapper.insert(segment);
            }

            // Notify progress
            videoTaskProducer.sendNotify(MqTopicConstant.TAG_TASK_PROGRESS,
                    Map.of("projectId", task.getProjectId(),
                            "taskId", task.getId(),
                            "type", task.getTaskType(),
                            "status", "SUCCESS"));

            // Check if all storyboards for the project are completed
            checkAllStoryboardsCompleted(task.getProjectId());

        } else {
            stateMachine.transition(task, TaskEvent.FAIL);
            task.setErrorMessage(errorMessage);
            task.setFinishedAt(java.time.LocalDateTime.now());
            videoTaskMapper.updateById(task);

            // Update storyboard status
            if (task.getStoryboardId() != null) {
                Storyboard storyboard = storyboardMapper.selectById(task.getStoryboardId());
                if (storyboard != null) {
                    storyboard.setStatus(4); // FAILED
                    storyboardMapper.updateById(storyboard);
                }
            }

            videoTaskProducer.sendNotify(MqTopicConstant.TAG_TASK_PROGRESS,
                    Map.of("projectId", task.getProjectId(),
                            "taskId", task.getId(),
                            "type", task.getTaskType(),
                            "status", "FAILED",
                            "errorMessage", errorMessage != null ? errorMessage : ""));
        }
    }

    /**
     * Check if all storyboards in the project have completed video generation.
     * If so, trigger compose task.
     */
    private void checkAllStoryboardsCompleted(Long projectId) {
        List<Storyboard> storyboards = storyboardMapper.selectList(
                new LambdaQueryWrapper<Storyboard>().eq(Storyboard::getProjectId, projectId)
        );

        boolean allCompleted = storyboards.stream()
                .allMatch(s -> s.getStatus() != null && s.getStatus() == 3);

        if (allCompleted && !storyboards.isEmpty()) {
            log.info("All storyboards completed for project {}, triggering compose", projectId);

            // Update project status
            Project project = projectMapper.selectById(projectId);
            if (project != null) {
                project.setStatus(1); // GENERATING
                projectMapper.updateById(project);
            }

            // Send compose task
            videoTaskProducer.sendComposeTask(Map.of("projectId", projectId));
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
