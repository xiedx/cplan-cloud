package com.cplan.creation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new project.
 */
@Data
public class CreateProjectRequest {

    @NotBlank(message = "项目标题不能为空")
    @Size(max = 256, message = "项目标题不能超过256字符")
    private String title;

    @Size(max = 1024, message = "项目描述不能超过1024字符")
    private String description;
}
