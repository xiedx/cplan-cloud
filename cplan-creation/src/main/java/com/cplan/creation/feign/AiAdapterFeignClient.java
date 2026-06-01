package com.cplan.creation.feign;

import com.cplan.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for communicating with AI Adapter Service.
 * Internal API — not exposed to end users.
 */
@FeignClient(
        name = "cplan-ai-adapter",
        path = "/internal/ai/v1",
        configuration = AiAdapterFeignConfig.class
)
public interface AiAdapterFeignClient {

    /**
     * Trigger script generation via AI Adapter.
     */
    @PostMapping("/script/generate")
    Result<Void> generateScript(@RequestBody Map<String, Object> request);

    /**
     * Trigger single-scene video generation via AI Adapter.
     */
    @PostMapping("/video/generate")
    Result<Void> generateVideo(@RequestBody Map<String, Object> request);
}
