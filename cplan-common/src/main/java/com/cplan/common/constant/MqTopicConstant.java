package com.cplan.common.constant;

/**
 * RocketMQ Topic and Tag name constants.
 *
 * <p>Naming convention:
 * <ul>
 *   <li>Topics use uppercase with underscore separators</li>
 *   <li>Consumer groups follow the pattern: {topic}-CONSUMER</li>
 * </ul>
 */
public final class MqTopicConstant {

    private MqTopicConstant() {
    }

    // ---- Topics ----

    /** Trigger script generation (Creation → AI Adapter) */
    public static final String TOPIC_SCRIPT_GEN = "CPLAN_SCRIPT_GEN";

    /** Script generation callback (AI Adapter → Creation) */
    public static final String TOPIC_SCRIPT_GEN_DONE = "CPLAN_SCRIPT_GEN_DONE";

    /** Trigger single-scene video generation (Creation → AI Adapter) */
    public static final String TOPIC_VIDEO_GEN = "CPLAN_VIDEO_GEN";

    /** Video generation callback (AI Adapter → Creation) */
    public static final String TOPIC_VIDEO_GEN_DONE = "CPLAN_VIDEO_GEN_DONE";

    /** Trigger video compose (Creation → Compose) */
    public static final String TOPIC_COMPOSE_TASK = "CPLAN_COMPOSE_TASK";

    /** Compose completion callback (Compose → Creation) */
    public static final String TOPIC_COMPOSE_DONE = "CPLAN_COMPOSE_DONE";

    /** Notification events (all services → Notify) */
    public static final String TOPIC_NOTIFY = "CPLAN_NOTIFY";

    // ---- Tags ----

    public static final String TAG_DEFAULT = "DEFAULT";
    public static final String TAG_TASK_PROGRESS = "TASK_PROGRESS";
    public static final String TAG_PROJECT_COMPLETE = "PROJECT_COMPLETE";

    // ---- Consumer Groups ----

    public static final String GROUP_SCRIPT_GEN = "CPLAN_SCRIPT_GEN-CONSUMER";
    public static final String GROUP_SCRIPT_GEN_DONE = "CPLAN_SCRIPT_GEN_DONE-CONSUMER";
    public static final String GROUP_VIDEO_GEN = "CPLAN_VIDEO_GEN-CONSUMER";
    public static final String GROUP_VIDEO_GEN_DONE = "CPLAN_VIDEO_GEN_DONE-CONSUMER";
    public static final String GROUP_COMPOSE_TASK = "CPLAN_COMPOSE_TASK-CONSUMER";
    public static final String GROUP_COMPOSE_DONE = "CPLAN_COMPOSE_DONE-CONSUMER";
    public static final String GROUP_NOTIFY = "CPLAN_NOTIFY-CONSUMER";
}
