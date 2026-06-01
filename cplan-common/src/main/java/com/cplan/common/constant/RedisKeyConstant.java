package com.cplan.common.constant;

/**
 * Redis key prefix constants.
 *
 * <p>Pattern: {@code {prefix}:{business-id}}
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
    }

    /** JWT token blacklist / active session set */
    public static final String PREFIX_TOKEN = "cplan:token:";

    /** User info cache */
    public static final String PREFIX_USER = "cplan:user:";

    /** Distributed lock prefix for video task processing */
    public static final String PREFIX_LOCK_TASK = "cplan:lock:task:";

    /** Rate limiter prefix */
    public static final String PREFIX_RATE_LIMIT = "cplan:rate:";

    /** SSE connection registry */
    public static final String PREFIX_SSE = "cplan:sse:";

    /** Build a full key from prefix and id */
    public static String key(String prefix, Object id) {
        return prefix + id;
    }
}
