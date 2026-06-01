package com.cplan.creation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for regenerating a storyboard video.
 * The videoPrompt is optional — if not provided, the existing prompt is used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegenerateStoryboardRequest {

    /** Optional override video prompt. */
    private String videoPrompt;
}
