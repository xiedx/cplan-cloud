package com.cplan.creation.statemachine;

/**
 * Events that can trigger a state transition on a VideoTask.
 */
public enum TaskEvent {
    /** Start processing the task. */
    START,
    /** Task completed successfully. */
    SUCCESS,
    /** Task failed. */
    FAIL,
    /** Retry the task after a failure. */
    RETRY
}
