package com.cplan.compose.controller;

import com.cplan.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Compose Service REST controller.
 * Provides task status query endpoint.
 */
@RestController
@RequestMapping("/api/compose/v1")
public class ComposeController {

    /**
     * Query compose task status.
     * GET /api/compose/v1/tasks/{taskId}/status
     */
    @GetMapping("/tasks/{taskId}/status")
    public Result<Map<String, Object>> getTaskStatus(@PathVariable Long taskId) {
        // Status is tracked via Creation Service's VideoTask table
        // This endpoint provides a lightweight status query
        return Result.ok(Map.of(
                "taskId", taskId,
                "status", "QUERY_VIA_CREATION_SERVICE"
        ));
    }
}
