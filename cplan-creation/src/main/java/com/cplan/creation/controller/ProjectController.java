package com.cplan.creation.controller;

import com.cplan.common.result.Result;
import com.cplan.creation.dto.CreateProjectRequest;
import com.cplan.creation.dto.ProjectVO;
import com.cplan.creation.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project REST controller.
 */
@RestController
@RequestMapping("/api/creation/v1/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Create a new project.
     * POST /api/creation/v1/projects
     */
    @PostMapping
    public Result<ProjectVO> createProject(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateProjectRequest req) {
        log.info("Create project: userId={}", userId);
        ProjectVO vo = projectService.createProject(Long.parseLong(userId), req);
        return Result.ok(vo);
    }

    /**
     * Get project details.
     * GET /api/creation/v1/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    public Result<ProjectVO> getProject(@PathVariable Long projectId) {
        ProjectVO vo = projectService.getProject(projectId);
        return Result.ok(vo);
    }
}
