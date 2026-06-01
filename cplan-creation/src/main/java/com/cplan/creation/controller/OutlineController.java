package com.cplan.creation.controller;

import com.cplan.common.result.Result;
import com.cplan.creation.dto.OutlineVO;
import com.cplan.creation.dto.SubmitOutlineRequest;
import com.cplan.creation.service.OutlineService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Outline REST controller.
 */
@RestController
@RequestMapping("/api/creation/v1/projects")
public class OutlineController {

    private static final Logger log = LoggerFactory.getLogger(OutlineController.class);

    private final OutlineService outlineService;

    public OutlineController(OutlineService outlineService) {
        this.outlineService = outlineService;
    }

    /**
     * Submit an outline for a project (triggers async script generation).
     * POST /api/creation/v1/projects/{projectId}/outline
     */
    @PostMapping("/{projectId}/outline")
    public Result<OutlineVO> submitOutline(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long projectId,
            @Valid @RequestBody SubmitOutlineRequest req) {
        log.info("Submit outline: userId={}, projectId={}", userId, projectId);
        OutlineVO vo = outlineService.submitOutline(Long.parseLong(userId), projectId, req);
        return Result.ok(vo);
    }
}
