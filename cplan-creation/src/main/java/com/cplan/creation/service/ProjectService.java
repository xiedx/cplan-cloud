package com.cplan.creation.service;

import com.cplan.creation.dto.CreateProjectRequest;
import com.cplan.creation.dto.ProjectVO;

/**
 * Project service interface.
 */
public interface ProjectService {

    /**
     * Create a new project.
     */
    ProjectVO createProject(Long userId, CreateProjectRequest req);

    /**
     * Get project by ID.
     */
    ProjectVO getProject(Long projectId);
}
