package com.cplan.ai.service;

/**
 * Video generation service interface.
 */
public interface VideoGenService {

    /**
     * Generate a video for the given prompt.
     *
     * @param videoPrompt     the text-to-video prompt
     * @param durationSeconds desired video duration in seconds
     * @return the URL of the generated video
     */
    String generateVideo(String videoPrompt, int durationSeconds);
}
