package com.taskmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("UP", "Task Manager API is running");
	}

	record HealthResponse(String status, String message) {
	}
}
