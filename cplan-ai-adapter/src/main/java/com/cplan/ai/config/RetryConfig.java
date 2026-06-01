package com.cplan.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Video generation API configuration — loaded from Nacos Config.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cplan.video-gen")
public class RetryConfig {

    /** Maximum retry attempts for AI API calls. */
    private int maxRetries = 3;

    /** Initial retry delay in milliseconds. */
    private long initialDelayMs = 1000;

    /** Multiplier for exponential backoff. */
    private double backoffMultiplier = 2.0;

    /** Maximum retry delay in milliseconds. */
    private long maxDelayMs = 30000;

    /** Video generation API endpoint. */
    private String endpoint = "https://api.jimeng.ai/v1/video/generate";

    /** API key for video generation service. */
    private String apiKey = "${CPLAN_VIDEO_GEN_API_KEY:sk-placeholder-change-in-nacos}";

    /** Polling interval for checking video task status (seconds). */
    private int pollingIntervalSeconds = 10;

    /** Maximum polling duration before timeout (seconds). */
    private int pollingTimeoutSeconds = 600;
}
