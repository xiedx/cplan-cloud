package com.cplan.creation.service.impl;

import com.cplan.common.exception.BizException;
import com.cplan.common.result.ResultCode;
import com.cplan.creation.dto.OutlineVO;
import com.cplan.creation.dto.SubmitOutlineRequest;
import com.cplan.creation.entity.Outline;
import com.cplan.creation.entity.Project;
import com.cplan.creation.entity.VideoTask;
import com.cplan.creation.feign.AiAdapterFeignClient;
import com.cplan.creation.mapper.OutlineMapper;
import com.cplan.creation.mapper.ProjectMapper;
import com.cplan.creation.mapper.VideoTaskMapper;
import com.cplan.creation.service.OutlineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Implementation of OutlineService.
 *
 * <p>On outline submission:
 * 1. Save the outline entity.
 * 2. Create a VideoTask (SCRIPT_GEN, PENDING).
 * 3. Call AI Adapter Feign to trigger async script generation.
 */
@Service
public class OutlineServiceImpl implements OutlineService {

    private static final Logger log = LoggerFactory.getLogger(OutlineServiceImpl.class);

    private final OutlineMapper outlineMapper;
    private final ProjectMapper projectMapper;
    private final VideoTaskMapper videoTaskMapper;
    private final AiAdapterFeignClient aiAdapterFeignClient;

    public OutlineServiceImpl(OutlineMapper outlineMapper,
                              ProjectMapper projectMapper,
                              VideoTaskMapper videoTaskMapper,
                              AiAdapterFeignClient aiAdapterFeignClient) {
        this.outlineMapper = outlineMapper;
        this.projectMapper = projectMapper;
        this.videoTaskMapper = videoTaskMapper;
        this.aiAdapterFeignClient = aiAdapterFeignClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutlineVO submitOutline(Long userId, Long projectId, SubmitOutlineRequest req) {
        // Verify project exists and belongs to user
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getUserId().equals(userId)) {
            throw new BizException(ResultCode.PROJECT_NOT_FOUND);
        }

        // Save outline
        Outline outline = Outline.builder()
                .projectId(projectId)
                .content(req.getContent())
                .status(1) // PROCESSING
                .isDeleted(0)
                .build();
        outlineMapper.insert(outline);

        // Update project status to GENERATING
        project.setStatus(1);
        projectMapper.updateById(project);

        // Create video task for script generation
        VideoTask videoTask = VideoTask.builder()
                .projectId(projectId)
                .taskType("SCRIPT_GEN")
                .taskStatus("PENDING")
                .retryCount(0)
                .isDeleted(0)
                .build();
        videoTaskMapper.insert(videoTask);

        // Call AI Adapter via Feign to trigger async script generation
        try {
            aiAdapterFeignClient.generateScript(Map.of(
                    "projectId", projectId,
                    "outlineId", outline.getId(),
                    "outlineContent", req.getContent(),
                    "llmModel", "deepseek-chat"
            ));
        } catch (Exception e) {
            log.error("Failed to call AI Adapter for script generation: {}", e.getMessage());
            // Don't roll back — the task is already persisted and can be retried
        }

        log.info("Outline submitted: projectId={}, outlineId={}, videoTaskId={}",
                projectId, outline.getId(), videoTask.getId());

        return OutlineVO.builder()
                .outlineId(outline.getId())
                .content(outline.getContent())
                .enrichedContent(outline.getEnrichedContent())
                .status(outline.getStatus())
                .build();
    }
}
