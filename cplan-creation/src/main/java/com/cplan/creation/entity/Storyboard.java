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
 * Storyboard entity — a single scene/shot from a script.
 *
 * <p>Status values:
 * <ul>
 *   <li>0 = PENDING_REVIEW (待审核)</li>
 *   <li>1 = CONFIRMED (已审核)</li>
 *   <li>2 = VIDEO_GENERATING (视频生成中)</li>
 *   <li>3 = VIDEO_COMPLETED (视频完成)</li>
 *   <li>4 = FAILED (失败)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_storyboard")
public class Storyboard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long scriptId;

    /** Scene sequence number within the script. */
    private Integer sequenceNo;

    private String sceneDescription;

    private String dialogue;

    private String imagePrompt;

    private String videoPrompt;

    /** 0=待审核, 1=已审核, 2=视频生成中, 3=视频完成, 4=失败 */
    private Integer status;

    /** URL of the generated video clip. */
    private String videoUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
