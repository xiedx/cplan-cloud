package com.cplan.common.result;

/**
 * Unified error/status code enumeration.
 *
 * <pre>
 * 200        - success
 * 400-499     - HTTP-level client errors
 * 500         - internal server error
 * 1000-1999   - user service business errors
 * 2000-2999   - creation service business errors
 * 3000-3999   - AI adapter errors
 * 4000-4999   - compose / storage errors
 * 5000-5999   - notify service errors
 * </pre>
 */
public enum ResultCode {

    // ---- Common ----
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ---- User Service (1000-1999) ----
    USER_EXIST(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    EMAIL_ALREADY_REGISTERED(1005, "邮箱已被注册"),

    // ---- Creation Service (2000-2999) ----
    PROJECT_NOT_FOUND(2001, "项目不存在"),
    STORYBOARD_NOT_CONFIRMED(2002, "分镜未审核"),
    OUTLINE_NOT_FOUND(2003, "大纲不存在"),
    SCRIPT_NOT_FOUND(2004, "剧本不存在"),
    STORYBOARD_NOT_FOUND(2005, "分镜不存在"),
    VIDEO_TASK_NOT_FOUND(2006, "视频任务不存在"),
    INVALID_STATE_TRANSITION(2007, "无效的状态流转"),

    // ---- AI Adapter Service (3000-3999) ----
    LLM_INVOKE_ERROR(3001, "LLM调用失败"),
    VIDEO_GEN_TIMEOUT(3002, "视频生成超时"),
    VIDEO_GEN_FAILED(3003, "视频生成失败"),
    SCRIPT_GEN_FAILED(3004, "剧本生成失败"),

    // ---- Compose / Storage Service (4000-4999) ----
    FFMPEG_ERROR(4001, "FFmpeg执行失败"),
    MINIO_UPLOAD_ERROR(4002, "MinIO上传失败"),
    MINIO_DOWNLOAD_ERROR(4003, "MinIO下载失败"),
    COMPOSE_FAILED(4004, "视频合成失败"),
    FILE_NOT_FOUND(4005, "文件不存在"),

    // ---- Notify Service (5000-5999) ----
    SSE_CONNECTION_ERROR(5001, "SSE连接异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
