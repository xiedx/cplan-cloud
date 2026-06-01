package com.cplan.creation.service;

import com.cplan.creation.dto.StoryboardVO;
import com.cplan.creation.dto.VideoTaskVO;

import java.util.List;

/**
 * Storyboard service interface.
 */
public interface StoryboardService {

    /**
     * List all storyboards for a project.
     */
    List<StoryboardVO> listStoryboards(Long projectId);

    /**
     * Confirm a storyboard, which triggers video generation for that scene.
     */
    VideoTaskVO confirmStoryboard(Long storyboardId);

    /**
     * Regenerate the video for a specific storyboard.
     */
    VideoTaskVO regenerateStoryboard(Long storyboardId, String videoPrompt);
}
