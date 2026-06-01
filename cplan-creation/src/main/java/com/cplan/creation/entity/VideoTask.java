package com.cplan.creation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VideoTask entity — tracks async AI processing tasks.
 *
 * <p>taskType values: SCRIPT_GEN, VIDEO_GEN, COMPOSE
 * <p>taskStatus values: PENDING, PROCESSING, SUCCESS, FAILED, RETRY
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_task")
public class VideoTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /** Nullable — compose tasks don't map to a single storyboard. */
    private Long storyboardId;

    /** SCRIPT_GEN | VIDEO_GEN | COMPOSE */
    private String taskType;

    /** PENDING | PROCESSING | SUCCESS | FAILED | RETRY */
    private String taskStatus;

    /** External AI platform task ID. */
    private String externalTaskId;

    private String errorMessage;

    @Builder.Default
    private Integer retryCount = 0;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
