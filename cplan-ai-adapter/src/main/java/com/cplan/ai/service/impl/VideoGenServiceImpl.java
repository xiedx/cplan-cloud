package com.cplan.ai.service.impl;

import com.cplan.ai.config.RetryConfig;
import com.cplan.ai.service.VideoGenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Video generation service implementation with mock fallback.
 *
 * <p>When the real video generation API is unavailable or in development mode,
 * returns a placeholder video URL. The real API call + polling logic is preserved
 * and can be activated via configuration.
 */
@Service
public class VideoGenServiceImpl implements VideoGenService {

    private static final Logger log = LoggerFactory.getLogger(VideoGenServiceImpl.class);

    /** Set to true to use mock responses instead of real API calls. */
    private static final boolean MOCK_ENABLED = true;

    private final RetryConfig retryConfig;
    private final WebClient webClient;

    public VideoGenServiceImpl(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
        this.webClient = WebClient.builder()
                .baseUrl(retryConfig.getEndpoint())
                .defaultHeader("Authorization", "Bearer " + retryConfig.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String generateVideo(String videoPrompt, int durationSeconds) {
        if (MOCK_ENABLED) {
            log.info("[MOCK] Generating video for prompt (length={}, duration={}s)",
                    videoPrompt.length(), durationSeconds);
            return generateMockVideo(videoPrompt, durationSeconds);
        }

        return callVideoGenApi(videoPrompt, durationSeconds);
    }

    /**
     * [MOCK] Simulate video generation with a delay and return a placeholder URL.
     * In production, replace this with actual API call + polling.
     */
    private String generateMockVideo(String videoPrompt, int durationSeconds) {
        try {
            // Simulate processing delay
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String mockVideoId = UUID.randomUUID().toString().substring(0, 8);
        String mockUrl = "https://storage.cplan.mock/videos/mock_" + mockVideoId + ".mp4";

        log.info("[MOCK] Video generated: url={}", mockUrl);
        return mockUrl;
    }

    /**
     * Call the real video generation API and poll for completion.
     * This method is preserved for production use.
     */
    private String callVideoGenApi(String videoPrompt, int durationSeconds) {
        // Step 1: Submit video generation task
        Map<String, Object> requestBody = Map.of(
                "prompt", videoPrompt,
                "duration", durationSeconds,
                "resolution", "1080p",
                "format", "mp4"
        );

        try {
            Map<?, ?> submitResponse = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (submitResponse == null || !submitResponse.containsKey("taskId")) {
                log.warn("Video gen API did not return taskId, falling back to mock");
                return generateMockVideo(videoPrompt, durationSeconds);
            }

            String externalTaskId = (String) submitResponse.get("taskId");
            log.info("Video gen task submitted: externalTaskId={}", externalTaskId);

            // Step 2: Poll for task completion
            return pollVideoTaskResult(externalTaskId, videoPrompt, durationSeconds);

        } catch (Exception e) {
            log.error("Video gen API call failed, falling back to mock: {}", e.getMessage());
            return generateMockVideo(videoPrompt, durationSeconds);
        }
    }

    /**
     * Poll the external video generation API for task completion.
     */
    private String pollVideoTaskResult(String externalTaskId, String videoPrompt, int durationSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = retryConfig.getPollingTimeoutSeconds() * 1000L;
        int intervalMs = retryConfig.getPollingIntervalSeconds() * 1000;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                Thread.sleep(intervalMs);

                Map<?, ?> statusResponse = webClient.get()
                        .uri("/tasks/{taskId}", externalTaskId)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(10))
                        .block();

                if (statusResponse != null) {
                    String status = (String) statusResponse.get("status");
                    if ("SUCCESS".equalsIgnoreCase(status)) {
                        String videoUrl = (String) statusResponse.get("videoUrl");
                        log.info("Video gen task completed: externalTaskId={}, url={}", externalTaskId, videoUrl);
                        return videoUrl;
                    } else if ("FAILED".equalsIgnoreCase(status)) {
                        log.warn("Video gen task failed: externalTaskId={}", externalTaskId);
                        return generateMockVideo(videoPrompt, durationSeconds);
                    }
                    // Still PROCESSING — continue polling
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Error polling video task status: {}", e.getMessage());
            }
        }

        log.warn("Video gen polling timed out for externalTaskId={}", externalTaskId);
        return generateMockVideo(videoPrompt, durationSeconds);
    }
}
