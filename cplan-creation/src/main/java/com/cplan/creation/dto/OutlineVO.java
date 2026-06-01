package com.cplan.creation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response VO for outline data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutlineVO {

    private Long outlineId;
    private String content;
    private String enrichedContent;
    /** 0=待处理, 1=处理中, 2=已完成 */
    private Integer status;
}
