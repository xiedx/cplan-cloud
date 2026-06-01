package com.cplan.creation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response VO for script data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptVO {

    private Long scriptId;
    private String content;
    private String llmModel;
    /** 0=生成中, 1=已完成, 2=失败 */
    private Integer status;
}
