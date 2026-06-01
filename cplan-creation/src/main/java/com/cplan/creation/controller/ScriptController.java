package com.cplan.creation.controller;

import com.cplan.common.result.Result;
import com.cplan.creation.dto.ScriptVO;
import com.cplan.creation.service.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Script REST controller.
 */
@RestController
@RequestMapping("/api/creation/v1/projects")
public class ScriptController {

    private static final Logger log = LoggerFactory.getLogger(ScriptController.class);

    private final ScriptService scriptService;

    public ScriptController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    /**
     * Get the generated script for a project.
     * GET /api/creation/v1/projects/{projectId}/script
     */
    @GetMapping("/{projectId}/script")
    public Result<ScriptVO> getScript(@PathVariable Long projectId) {
        log.debug("Get script: projectId={}", projectId);
        ScriptVO vo = scriptService.getScript(projectId);
        return Result.ok(vo);
    }
}
