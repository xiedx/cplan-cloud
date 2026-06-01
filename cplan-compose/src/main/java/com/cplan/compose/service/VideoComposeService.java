package com.cplan.compose.service;

/**
 * Video compose service interface.
 */
public interface VideoComposeService {

    /**
     * Concatenate multiple video segments into a final video.
     *
     * @param projectId   the project ID
     * @param videoUrls   list of video segment URLs
     * @return URL of the final composed video
     */
    String composeVideos(Long projectId, java.util.List<String> videoUrls);
}
