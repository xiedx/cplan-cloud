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
 * VideoSegment entity — a rendered video clip for a storyboard.
 *
 * <p>status values:
 * <ul>
 *   <li>0 = UPLOADING (上传中)</li>
 *   <li>1 = AVAILABLE (可用)</li>
 *   <li>2 = DELETED (删除)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_segment")
public class VideoSegment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long storyboardId;

    /** MinIO object key. */
    private String fileKey;

    /** Accessible URL for the video segment. */
    private String fileUrl;

    /** Duration in milliseconds. */
    private Integer durationMs;

    /** File size in bytes. */
    private Long fileSize;

    /** 0=上传中, 1=可用, 2=删除 */
    private Integer status;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer isDeleted;
}
