package com.cplan.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM API configuration — loaded from Nacos Config (cplan-ai-adapter.yaml).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cplan.llm")
public class LlmConfig {

    /** LLM API endpoint (e.g. DeepSeek, OpenAI-compatible). */
    private String endpoint = "https://api.deepseek.com/v1/chat/completions";

    /** API key for LLM service. */
    private String apiKey = "${CPLAN_LLM_API_KEY:sk-placeholder-change-in-nacos}";

    /** Model name to use. */
    private String model = "deepseek-chat";

    /** Request timeout in seconds. */
    private int timeoutSeconds = 120;

    /** Maximum tokens in the response. */
    private int maxTokens = 4096;
}
