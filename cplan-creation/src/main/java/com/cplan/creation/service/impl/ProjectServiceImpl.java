package com.cplan.creation.service.impl;

import com.cplan.common.exception.BizException;
import com.cplan.common.result.ResultCode;
import com.cplan.creation.dto.CreateProjectRequest;
import com.cplan.creation.dto.ProjectVO;
import com.cplan.creation.entity.Project;
import com.cplan.creation.mapper.ProjectMapper;
import com.cplan.creation.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProjectService.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectVO createProject(Long userId, CreateProjectRequest req) {
        Project project = Project.builder()
                .userId(userId)
                .title(req.getTitle())
                .description(req.getDescription())
                .status(0) // DRAFT
                .isDeleted(0)
                .build();
        projectMapper.insert(project);
        log.info("Created project: id={}, userId={}", project.getId(), userId);

        return ProjectVO.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .finalVideoUrl(null)
                .build();
    }

    @Override
    public ProjectVO getProject(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ResultCode.PROJECT_NOT_FOUND);
        }
        return ProjectVO.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .finalVideoUrl(project.getFinalVideoUrl())
                .build();
    }
}
