package com.cplan.creation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for submitting an outline.
 */
@Data
public class SubmitOutlineRequest {

    @NotBlank(message = "大纲内容不能为空")
    private String content;
}
