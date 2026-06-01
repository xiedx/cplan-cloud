package com.cplan.creation.controller;

import com.cplan.common.result.Result;
import com.cplan.creation.dto.RegenerateStoryboardRequest;
import com.cplan.creation.dto.StoryboardVO;
import com.cplan.creation.dto.VideoTaskVO;
import com.cplan.creation.service.StoryboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Storyboard REST controller.
 */
@RestController
@RequestMapping("/api/creation/v1")
public class StoryboardController {

    private static final Logger log = LoggerFactory.getLogger(StoryboardController.class);

    private final StoryboardService storyboardService;

    public StoryboardController(StoryboardService storyboardService) {
        this.storyboardService = storyboardService;
    }

    /**
     * List all storyboards for a project.
     * GET /api/creation/v1/projects/{projectId}/storyboards
     */
    @GetMapping("/projects/{projectId}/storyboards")
    public Result<List<StoryboardVO>> listStoryboards(@PathVariable Long projectId) {
        log.debug("List storyboards: projectId={}", projectId);
        List<StoryboardVO> list = storyboardService.listStoryboards(projectId);
        return Result.ok(list);
    }

    /**
     * Confirm a storyboard (triggers video generation).
     * POST /api/creation/v1/storyboards/{storyboardId}/confirm
     */
    @PostMapping("/storyboards/{storyboardId}/confirm")
    public Result<VideoTaskVO> confirmStoryboard(@PathVariable Long storyboardId) {
        log.info("Confirm storyboard: id={}", storyboardId);
        VideoTaskVO vo = storyboardService.confirmStoryboard(storyboardId);
        return Result.ok(vo);
    }

    /**
     * Regenerate video for a storyboard.
     * POST /api/creation/v1/storyboards/{storyboardId}/regenerate
     */
    @PostMapping("/storyboards/{storyboardId}/regenerate")
    public Result<VideoTaskVO> regenerateStoryboard(
            @PathVariable Long storyboardId,
            @RequestBody(required = false) RegenerateStoryboardRequest req) {
        String prompt = (req != null) ? req.getVideoPrompt() : null;
        log.info("Regenerate storyboard: id={}, prompt={}", storyboardId, prompt != null ? "provided" : "default");
        VideoTaskVO vo = storyboardService.regenerateStoryboard(storyboardId, prompt);
        return Result.ok(vo);
    }
}
