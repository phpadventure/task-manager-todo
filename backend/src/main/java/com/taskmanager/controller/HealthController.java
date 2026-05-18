package com.taskmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    @Operation(summary = "Health check", description = "Returns API health status")
    @ApiResponse(
        responseCode = "200",
        description = "API is operational",
        content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "Task Manager API is running");
    }

    record HealthResponse(String status, String message) {
    }
}
