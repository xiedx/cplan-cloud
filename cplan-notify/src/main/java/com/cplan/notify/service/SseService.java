package com.cplan.notify.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE service interface for managing server-sent event connections.
 */
public interface SseService {

    /**
     * Subscribe a user to SSE events.
     *
     * @param userId the user ID
     * @return SseEmitter for the subscription
     */
    SseEmitter subscribe(Long userId);

    /**
     * Push an event to a specific user.
     *
     * @param userId the target user ID
     * @param eventName the SSE event name (e.g., "task_progress", "project_complete")
     * @param data     the event data (will be serialized to JSON)
     */
    void pushEvent(Long userId, String eventName, Object data);

    /**
     * Remove a user's SSE connection.
     *
     * @param userId the user ID
     */
    void remove(Long userId);
}
