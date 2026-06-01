package com.cplan.creation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cplan.common.exception.BizException;
import com.cplan.common.result.ResultCode;
import com.cplan.creation.dto.StoryboardVO;
import com.cplan.creation.dto.VideoTaskVO;
import com.cplan.creation.entity.Storyboard;
import com.cplan.creation.entity.VideoTask;
import com.cplan.creation.feign.AiAdapterFeignClient;
import com.cplan.creation.mapper.StoryboardMapper;
import com.cplan.creation.mapper.VideoTaskMapper;
import com.cplan.creation.service.StoryboardService;
import com.cplan.creation.statemachine.TaskEvent;
import com.cplan.creation.statemachine.VideoTaskStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of StoryboardService.
 */
@Service
public class StoryboardServiceImpl implements StoryboardService {

    private static final Logger log = LoggerFactory.getLogger(StoryboardServiceImpl.class);

    private final StoryboardMapper storyboardMapper;
    private final VideoTaskMapper videoTaskMapper;
    private final AiAdapterFeignClient aiAdapterFeignClient;
    private final VideoTaskStateMachine stateMachine;

    public StoryboardServiceImpl(StoryboardMapper storyboardMapper,
                                 VideoTaskMapper videoTaskMapper,
                                 AiAdapterFeignClient aiAdapterFeignClient,
                                 VideoTaskStateMachine stateMachine) {
        this.storyboardMapper = storyboardMapper;
        this.videoTaskMapper = videoTaskMapper;
        this.aiAdapterFeignClient = aiAdapterFeignClient;
        this.stateMachine = stateMachine;
    }

    @Override
    public List<StoryboardVO> listStoryboards(Long projectId) {
        List<Storyboard> storyboards = storyboardMapper.selectList(
                new LambdaQueryWrapper<Storyboard>()
                        .eq(Storyboard::getProjectId, projectId)
                        .orderByAsc(Storyboard::getSequenceNo)
        );
        return storyboards.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoTaskVO confirmStoryboard(Long storyboardId) {
        Storyboard storyboard = storyboardMapper.selectById(storyboardId);
        if (storyboard == null) {
            throw new BizException(ResultCode.STORYBOARD_NOT_FOUND);
        }

        // Update storyboard status to CONFIRMED
        storyboard.setStatus(1); // CONFIRMED
        storyboardMapper.updateById(storyboard);

        // Create VIDEO_GEN task
        VideoTask videoTask = VideoTask.builder()
                .projectId(storyboard.getProjectId())
                .storyboardId(storyboardId)
                .taskType("VIDEO_GEN")
                .taskStatus("PENDING")
                .retryCount(0)
                .isDeleted(0)
                .build();
        videoTaskMapper.insert(videoTask);

        // Trigger AI video generation via Feign
        try {
            aiAdapterFeignClient.generateVideo(Map.of(
                    "projectId", storyboard.getProjectId(),
                    "storyboardId", storyboardId,
                    "videoTaskId", videoTask.getId(),
                    "videoPrompt", storyboard.getVideoPrompt() != null ? storyboard.getVideoPrompt() : "",
                    "durationSeconds", 5
            ));
        } catch (Exception e) {
            log.error("Failed to call AI Adapter for video generation: {}", e.getMessage());
        }

        log.info("Storyboard confirmed: id={}, videoTaskId={}", storyboardId, videoTask.getId());

        return VideoTaskVO.builder()
                .taskId(videoTask.getId())
                .taskType(videoTask.getTaskType())
                .taskStatus(videoTask.getTaskStatus())
                .errorMessage(null)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoTaskVO regenerateStoryboard(Long storyboardId, String videoPrompt) {
        Storyboard storyboard = storyboardMapper.selectById(storyboardId);
        if (storyboard == null) {
            throw new BizException(ResultCode.STORYBOARD_NOT_FOUND);
        }

        // Override video prompt if provided
        if (videoPrompt != null && !videoPrompt.isBlank()) {
            storyboard.setVideoPrompt(videoPrompt);
        }

        // Reset storyboard status
        storyboard.setStatus(0); // PENDING_REVIEW
        storyboard.setVideoUrl(null);
        storyboardMapper.updateById(storyboard);

        // Create new VIDEO_GEN task
        VideoTask videoTask = VideoTask.builder()
                .projectId(storyboard.getProjectId())
                .storyboardId(storyboardId)
                .taskType("VIDEO_GEN")
                .taskStatus("PENDING")
                .retryCount(0)
                .isDeleted(0)
                .build();
        videoTaskMapper.insert(videoTask);

        // Trigger AI video generation via Feign
        try {
            aiAdapterFeignClient.generateVideo(Map.of(
                    "projectId", storyboard.getProjectId(),
                    "storyboardId", storyboardId,
                    "videoTaskId", videoTask.getId(),
                    "videoPrompt", storyboard.getVideoPrompt() != null ? storyboard.getVideoPrompt() : "",
                    "durationSeconds", 5
            ));
        } catch (Exception e) {
            log.error("Failed to call AI Adapter for video regeneration: {}", e.getMessage());
        }

        log.info("Storyboard regeneration triggered: id={}, videoTaskId={}", storyboardId, videoTask.getId());

        return VideoTaskVO.builder()
                .taskId(videoTask.getId())
                .taskType(videoTask.getTaskType())
                .taskStatus(videoTask.getTaskStatus())
                .errorMessage(null)
                .build();
    }

    private StoryboardVO toVO(Storyboard entity) {
        return StoryboardVO.builder()
                .id(entity.getId())
                .sequenceNo(entity.getSequenceNo())
                .sceneDescription(entity.getSceneDescription())
                .dialogue(entity.getDialogue())
                .imagePrompt(entity.getImagePrompt())
                .videoPrompt(entity.getVideoPrompt())
                .status(entity.getStatus())
                .videoUrl(entity.getVideoUrl())
                .build();
    }
}
