package com.taskmanager.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.dto.ErrorResponse;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.dto.UpdateTaskRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

  private final TaskService taskService;

  @Operation(
      summary = "Get all tasks for user",
      description = "Returns all tasks for the authenticated user with optional filtering")
  @ApiResponses(
      value = {
          @ApiResponse(
              responseCode = "200",
              description = "Tasks retrieved successfully",
              content = @Content(schema = @Schema(implementation = TaskDTO.class))),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid filter parameters",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
              responseCode = "401",
              description = "User not authenticated",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping
  public ResponseEntity<List<TaskDTO>> getAllTasks(
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          TaskStatus status,
      @Parameter(description = "Filter by priority") @RequestParam(required = false)
          TaskPriority priority,
      @Parameter(description = "Search query for title/description")
      @RequestParam(required = false) String q) {
    UUID userId = getCurrentUserId();

    List<Task> tasks;
    if (q != null && !q.isEmpty()) {
      tasks = taskService.searchTasks(userId, q);
    } else if (status != null && priority != null) {
      tasks = taskService.filterByStatus(userId, status).stream()
          .filter(t -> t.getPriority() == priority)
          .toList();
    } else if (status != null) {
      tasks = taskService.filterByStatus(userId, status);
    } else if (priority != null) {
      tasks = taskService.filterByPriority(userId, priority);
    } else {
      tasks = taskService.getAllTasksForUser(userId);
    }

    return ResponseEntity.ok(tasks.stream().map(TaskDTO::fromEntity).toList());
  }

  @Operation(
      summary = "Get tasks for specific list",
      description = "Returns all tasks in a specific list")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
          @ApiResponse(
              responseCode = "401",
              description = "User not authenticated",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
              responseCode = "403",
              description = "User does not own this list",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
          @ApiResponse(
              responseCode = "404",
              description = "List not found",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/lists/{listId}")
  public ResponseEntity<List<TaskDTO>> getTasksByList(
      @Parameter(description = "List ID") @PathVariable UUID listId) {
    UUID userId = getCurrentUserId();
    List<Task> tasks = taskService.getTasksByList(listId, userId);
    return ResponseEntity.ok(tasks.stream().map(TaskDTO::fromEntity).toList());
  }

  @Operation(summary = "Get single task", description = "Returns details of a specific task")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{taskId}")
  public ResponseEntity<TaskDTO> getTask(
      @Parameter(description = "Task ID") @PathVariable UUID taskId) {
    UUID userId = getCurrentUserId();
    Task task = taskService.getTask(taskId, userId);
    return ResponseEntity.ok(TaskDTO.fromEntity(task));
  }

  @Operation(summary = "Create new task", description = "Create a new task in the specified list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Task created successfully"),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid input",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this list",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "List not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/lists/{listId}")
  public ResponseEntity<TaskDTO> createTask(
      @Parameter(description = "List ID") @PathVariable UUID listId,
      @Valid @RequestBody CreateTaskRequest request) {
    UUID userId = getCurrentUserId();
    Task task = taskService.createTask(userId, listId, request);
    return ResponseEntity
        .created(URI.create("/api/tasks/" + task.getId()))
        .body(TaskDTO.fromEntity(task));
  }

  @Operation(summary = "Update task", description = "Update details of an existing task")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task updated successfully"),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid input",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/{taskId}")
  public ResponseEntity<TaskDTO> updateTask(
      @Parameter(description = "Task ID") @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskRequest request) {
    UUID userId = getCurrentUserId();
    Task task = taskService.updateTask(taskId, userId, request);
    return ResponseEntity.ok(TaskDTO.fromEntity(task));
  }

  @Operation(summary = "Update task status", description = "Quick update of task status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task status updated successfully"),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid status value",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping("/{taskId}/status")
  public ResponseEntity<TaskDTO> updateTaskStatus(
      @Parameter(description = "Task ID") @PathVariable UUID taskId,
      @Parameter(description = "New status") @RequestParam TaskStatus status) {
    UUID userId = getCurrentUserId();
    Task task = taskService.updateTaskStatus(taskId, userId, status);
    return ResponseEntity.ok(TaskDTO.fromEntity(task));
  }

  @Operation(summary = "Delete task", description = "Delete a task permanently")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteTask(
      @Parameter(description = "Task ID") @PathVariable UUID taskId) {
    UUID userId = getCurrentUserId();
    taskService.deleteTask(taskId, userId);
    return ResponseEntity.noContent().build();
  }

  private UUID getCurrentUserId() {
    var principal = SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    if (principal instanceof SecurityUser securityUser) {
      return securityUser.getId();
    }
    throw new IllegalStateException("User not authenticated");
  }
}
