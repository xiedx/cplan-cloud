package com.cplan.creation.service;

import com.cplan.creation.dto.ScriptVO;

/**
 * Script service interface.
 */
public interface ScriptService {

    /**
     * Retrieve the generated script for a project.
     *
     * @param projectId the project ID
     * @return script view object
     */
    ScriptVO getScript(Long projectId);
}
