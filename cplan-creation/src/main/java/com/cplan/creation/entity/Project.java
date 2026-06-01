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
 * Project entity — represents a user's video project.
 *
 * <p>Status values:
 * <ul>
 *   <li>0 = DRAFT (草稿)</li>
 *   <li>1 = GENERATING (生成中)</li>
 *   <li>2 = COMPLETED (已完成)</li>
 *   <li>3 = FAILED (失败)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    /** 0=草稿, 1=生成中, 2=已完成, 3=失败 */
    private Integer status;

    private String finalVideoUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
