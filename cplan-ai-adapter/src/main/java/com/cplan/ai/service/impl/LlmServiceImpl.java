package com.cplan.ai.service.impl;

import com.cplan.ai.config.LlmConfig;
import com.cplan.ai.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * LLM service implementation with mock fallback.
 *
 * <p>When the real LLM API is unavailable or in development mode,
 * returns mock script content. The real API call logic is preserved
 * and can be activated via configuration.
 */
@Service
public class LlmServiceImpl implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmServiceImpl.class);

    /** Set to true to use mock responses instead of real API calls. */
    private static final boolean MOCK_ENABLED = true;

    private final LlmConfig llmConfig;
    private final WebClient webClient;

    public LlmServiceImpl(LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
        this.webClient = WebClient.builder()
                .baseUrl(llmConfig.getEndpoint())
                .defaultHeader("Authorization", "Bearer " + llmConfig.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String generateScript(String outlineContent, String llmModel) {
        if (MOCK_ENABLED) {
            log.info("[MOCK] Generating script for outline (length={})", outlineContent.length());
            return generateMockScript(outlineContent);
        }

        return callLlmApi(outlineContent, llmModel);
    }

    /**
     * [MOCK] Generate a placeholder script based on the outline.
     * In production, replace this with actual API call.
     */
    private String generateMockScript(String outlineContent) {
        String model = (llmModel != null && !llmModel.isBlank()) ? llmModel : llmConfig.getModel();

        // [MOCK] Simulated script output
        return """
                [MOCK] 以下为模拟生成的剧本内容（模型: %s）

                ===== 第一幕：开场 =====
                场景描述：城市黄昏，霓虹灯渐次亮起，镜头从天际线缓缓下降至街头。
                台词（旁白）：在这个被数据重塑的时代，每一个像素都在讲述新的故事。
                画面提示：cinematic city skyline at dusk, neon lights, 4K quality, slow zoom

                ===== 第二幕：发展 =====
                场景描述：实验室内部，全息屏幕上流淌着代码，科研人员专注工作的侧影。
                台词（旁白）：从图灵测试到深度学习，人工智能的每一次进化都伴随着质疑与突破。
                画面提示：futuristic lab interior, holographic screens, focused researchers, sci-fi lighting

                ===== 第三幕：高潮 =====
                场景描述：AI 与人类并肩面对巨大全息数据墙，双手触碰屏幕，光芒四射。
                台词（旁白）：当算法理解了情感，当代码学会了共情——这不是终结，而是新的开始。
                画面提示：human and AI standing together, holographic data wall, dramatic lighting, 4K

                ===== 第四幕：结尾 =====
                场景描述：清晨阳光透过窗户，一个人在桌前微笑着合上笔记本电脑。
                台词（旁白）：技术的意义不在于替代，而在于让我们成为更好的自己。
                画面提示：morning sunlight through window, person closing laptop with smile, warm tones
                """.formatted(model);
    }

    /**
     * Call the real LLM API (DeepSeek / OpenAI-compatible).
     * This method is preserved for production use.
     */
    private String callLlmApi(String outlineContent, String llmModel) {
        String model = (llmModel != null && !llmModel.isBlank()) ? llmModel : llmConfig.getModel();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content", outlineContent)
                ),
                "max_tokens", llmConfig.getMaxTokens()
        );

        try {
            Map<?, ?> response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(llmConfig.getTimeoutSeconds()))
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            log.warn("Unexpected LLM API response structure");
            return generateMockScript(outlineContent);
        } catch (Exception e) {
            log.error("LLM API call failed, falling back to mock: {}", e.getMessage());
            return generateMockScript(outlineContent);
        }
    }

    private String buildSystemPrompt() {
        return """
                你是一位专业的短视频编剧。根据用户提供的大纲，生成完整的短视频剧本。
                
                要求：
                1. 将大纲拆分为3-5个分镜场景
                2. 每个场景包含：场景描述、台词/旁白、画面提示词（英文，用于AI视频生成）
                3. 画面提示词需包含：镜头语言、主体描述、光影效果、分辨率要求
                4. 总时长控制在30-60秒
                5. 风格统一，节奏紧凑
                
                输出格式：
                ===== 第X幕：标题 =====
                场景描述：...
                台词（旁白）：...
                画面提示：...
                """;
    }
}
