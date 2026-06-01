package com.cplan.creation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response VO for project data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {

    private Long projectId;
    private String title;
    private String description;
    /** 0=草稿, 1=生成中, 2=已完成, 3=失败 */
    private Integer status;
    private String finalVideoUrl;
}
