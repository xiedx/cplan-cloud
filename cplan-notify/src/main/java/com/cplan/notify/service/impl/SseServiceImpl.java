package com.cplan.notify.service.impl;

import com.cplan.notify.service.SseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory SSE service implementation.
 * Manages user connections via ConcurrentHashMap.
 *
 * <p>In production, consider using Redis pub/sub for multi-instance support.
 */
@org.springframework.stereotype.Service
public class SseServiceImpl implements SseService {

    private static final Logger log = LoggerFactory.getLogger(SseServiceImpl.class);

    /** SSE connection timeout: 30 minutes. */
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    /** Active SSE connections per user. */
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public SseServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SseEmitter subscribe(Long userId) {
        // Close existing connection if any
        remove(userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> {
            log.debug("SSE connection completed: userId={}", userId);
            emitters.remove(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE connection timed out: userId={}", userId);
            emitters.remove(userId, emitter);
        });

        emitter.onError(ex -> {
            log.warn("SSE connection error: userId={}, error={}", userId, ex.getMessage());
            emitters.remove(userId, emitter);
        });

        emitters.put(userId, emitter);
        log.info("SSE subscription established: userId={}", userId);

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"SSE connection established\"}")
                    .reconnectTime(5000));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event: userId={}", userId);
        }

        return emitter;
    }

    @Override
    public void pushEvent(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("No active SSE connection for userId={}, skipping event: {}", userId, eventName);
            return;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(jsonData));
            log.debug("SSE event sent: userId={}, event={}", userId, eventName);
        } catch (IOException e) {
            log.warn("Failed to send SSE event: userId={}, event={}, error={}",
                    userId, eventName, e.getMessage());
            emitters.remove(userId, emitter);
        }
    }

    @Override
    public void remove(Long userId) {
        SseEmitter old = emitters.remove(userId);
        if (old != null) {
            old.complete();
            log.debug("SSE connection removed: userId={}", userId);
        }
    }
}
