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
 * Outline entity — the user-provided outline that triggers script generation.
 *
 * <p>Status values:
 * <ul>
 *   <li>0 = PENDING (待处理)</li>
 *   <li>1 = PROCESSING (处理中)</li>
 *   <li>2 = COMPLETED (已完成)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_outline")
public class Outline implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /** User-submitted original outline text. */
    private String content;

    /** AI-enriched outline content. */
    private String enrichedContent;

    /** 0=待处理, 1=处理中, 2=已完成 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
