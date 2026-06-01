package com.cplan.creation.statemachine;

import com.cplan.common.exception.BizException;
import com.cplan.common.result.ResultCode;
import com.cplan.creation.entity.VideoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * State machine governing VideoTask status transitions.
 *
 * <pre>
 *   PENDING    --START-->     PROCESSING
 *   PROCESSING --SUCCESS-->   SUCCESS
 *   PROCESSING --FAIL-->      FAILED
 *   FAILED     --RETRY-->     PENDING
 *   RETRY      --START-->     PROCESSING
 * </pre>
 */
@Component
public class VideoTaskStateMachine {

    private static final Logger log = LoggerFactory.getLogger(VideoTaskStateMachine.class);

    /**
     * Valid transitions: (currentStatus, event) → newStatus.
     */
    private static final Map<String, Map<TaskEvent, String>> TRANSITIONS = Map.of(
            "PENDING", Map.of(TaskEvent.START, "PROCESSING"),
            "PROCESSING", Map.of(
                    TaskEvent.SUCCESS, "SUCCESS",
                    TaskEvent.FAIL, "FAILED"
            ),
            "FAILED", Map.of(TaskEvent.RETRY, "PENDING"),
            "RETRY", Map.of(TaskEvent.START, "PROCESSING")
    );

    /**
     * Attempt a state transition on the given task.
     *
     * @param task  the video task to transition
     * @param event the triggering event
     * @throws BizException if the transition is not allowed
     */
    public void transition(VideoTask task, TaskEvent event) {
        String currentStatus = task.getTaskStatus();
        if (!canTransit(currentStatus, event)) {
            log.warn("Invalid state transition: task={}, currentStatus={}, event={}",
                    task.getId(), currentStatus, event);
            throw new BizException(ResultCode.INVALID_STATE_TRANSITION);
        }

        String newStatus = TRANSITIONS.get(currentStatus).get(event);
        task.setTaskStatus(newStatus);

        // Update timestamps
        if (event == TaskEvent.START) {
            task.setStartedAt(LocalDateTime.now());
        } else if (event == TaskEvent.SUCCESS || event == TaskEvent.FAIL) {
            task.setFinishedAt(LocalDateTime.now());
        }
        if (event == TaskEvent.RETRY) {
            task.setRetryCount(task.getRetryCount() + 1);
        }

        log.info("VideoTask state transition: id={}, {} --{}--> {}",
                task.getId(), currentStatus, event, newStatus);
    }

    /**
     * Check if a transition from the given status via the event is valid.
     */
    public boolean canTransit(String currentStatus, TaskEvent event) {
        Map<TaskEvent, String> transitions = TRANSITIONS.get(currentStatus);
        return transitions != null && transitions.containsKey(event);
    }
}
