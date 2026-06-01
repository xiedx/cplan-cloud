package com.cplan.notify.controller;

import com.cplan.notify.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE controller — manages real-time event subscriptions.
 */
@RestController
@RequestMapping("/api/notify/v1/sse")
public class SseController {

    private static final Logger log = LoggerFactory.getLogger(SseController.class);

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Subscribe to SSE events for the authenticated user.
     * GET /api/notify/v1/sse/subscribe
     *
     * <p>The X-User-Id header is injected by the API Gateway after JWT validation.
     * This path is whitelisted in the gateway to allow SSE connection establishment.
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            log.warn("SSE subscribe request without X-User-Id header");
            // Return a short-lived emitter that will be closed immediately
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new RuntimeException("Unauthorized"));
            return emitter;
        }

        log.info("SSE subscribe: userId={}", userId);
        return sseService.subscribe(Long.parseLong(userId));
    }
}
