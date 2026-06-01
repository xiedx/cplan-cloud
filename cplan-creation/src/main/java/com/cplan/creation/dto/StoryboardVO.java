package com.cplan.creation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response VO for storyboard data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryboardVO {

    private Long id;
    private Integer sequenceNo;
    private String sceneDescription;
    private String dialogue;
    private String imagePrompt;
    private String videoPrompt;
    /** 0=待审核, 1=已审核, 2=视频生成中, 3=视频完成, 4=失败 */
    private Integer status;
    private String videoUrl;
}
