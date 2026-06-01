package com.cplan.compose.service.impl;

import com.cplan.compose.service.VideoComposeService;
import com.cplan.compose.util.FFmpegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of VideoComposeService.
 *
 * <p>Currently uses mock implementation for development.
 * Production implementation will use FFmpegUtil to perform actual video concatenation.
 */
@Service
public class VideoComposeServiceImpl implements VideoComposeService {

    private static final Logger log = LoggerFactory.getLogger(VideoComposeServiceImpl.class);

    /** Set to true to use mock responses instead of real FFmpeg calls. */
    private static final boolean MOCK_ENABLED = true;

    private final FFmpegUtil ffmpegUtil;

    public VideoComposeServiceImpl(FFmpegUtil ffmpegUtil) {
        this.ffmpegUtil = ffmpegUtil;
    }

    @Override
    public String composeVideos(Long projectId, List<String> videoUrls) {
        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new IllegalArgumentException("Video URLs list cannot be empty");
        }

        log.info("Composing {} video segments for project {}", videoUrls.size(), projectId);

        if (MOCK_ENABLED) {
            return composeMock(projectId, videoUrls);
        }

        // Real FFmpeg-based composition
        return composeWithFFmpeg(projectId, videoUrls);
    }

    /**
     * [MOCK] Simulate video composition.
     * In production, replace with actual FFmpeg processing.
     */
    private String composeMock(Long projectId, List<String> videoUrls) {
        log.info("[MOCK] Composing {} video segments for project {}", videoUrls.size(), projectId);

        // Simulate processing time
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String mockId = UUID.randomUUID().toString().substring(0, 8);
        String finalUrl = "https://storage.cplan.mock/finals/project_" + projectId + "_final_" + mockId + ".mp4";

        log.info("[MOCK] Composed video: url={}", finalUrl);
        return finalUrl;
    }

    /**
     * Real FFmpeg-based video composition.
     * This method is preserved for production use.
     */
    private String composeWithFFmpeg(Long projectId, List<String> videoUrls) {
        try {
            // Step 1: Download all video segments to local temp directory
            String tempDir = System.getProperty("java.io.tmpdir") + "/cplan-compose/" + projectId;
            java.io.File tempDirFile = new java.io.File(tempDir);
            if (!tempDirFile.exists()) {
                tempDirFile.mkdirs();
            }

            // Step 2: Create FFmpeg concat input file
            String concatFilePath = tempDir + "/concat_list.txt";
            String outputPath = tempDir + "/final_" + projectId + ".mp4";

            // TODO: Download video segments from MinIO to local temp files
            // For each video URL, download to tempDir/segment_N.mp4
            // Then create concat list file

            // Step 3: Use FFmpegUtil to concatenate
            List<String> localPaths = videoUrls; // Replace with local paths after download
            ffmpegUtil.concatVideos(localPaths, outputPath);

            // Step 4: Upload final video to MinIO
            // TODO: Upload outputPath to MinIO cplan-finals bucket
            // Return the MinIO URL

            String finalUrl = "https://storage.cplan.cloud/finals/project_" + projectId + "/final.mp4";
            log.info("Composed video with FFmpeg: url={}", finalUrl);
            return finalUrl;

        } catch (Exception e) {
            log.error("FFmpeg composition failed for project {}: {}", projectId, e.getMessage());
            throw new RuntimeException("Video composition failed", e);
        }
    }
}
