package com.cplan.creation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response VO for video task data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskVO {

    private Long taskId;
    private String taskType;
    private String taskStatus;
    private String errorMessage;
}
