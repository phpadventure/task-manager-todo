package com.taskmanager.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.dto.ErrorResponse;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.dto.UpdateTaskRequest;
import com.taskmanager.entity.DueDateFilter;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.service.SecurityContextService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

  private final TaskService taskService;
  private final SecurityContextService securityContextService;

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
  @GetMapping("/tasks")
  public ResponseEntity<List<TaskDTO>> getAllTasks(
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          TaskStatus status,
      @Parameter(description = "Filter by priority") @RequestParam(required = false)
          TaskPriority priority,
      @Parameter(description = "Filter by due date (overdue, today, next_7_days, all)")
      @RequestParam(required = false) String dueDateFilter) {
    UUID userId = securityContextService.getCurrentUserId();

    List<Task> tasks;
    if (status != null && priority != null) {
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

    DueDateFilter dateFilter = DueDateFilter.fromValue(dueDateFilter);
    tasks = filterByDueDate(tasks, dateFilter);
    return ResponseEntity.ok(tasks.stream().map(TaskDTO::fromEntity).toList());
  }

  @Operation(
      summary = "Search tasks",
      description = "Search all user's tasks by title and description")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Search results returned"),
          @ApiResponse(
              responseCode = "401",
              description = "User not authenticated",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/tasks/search")
  public ResponseEntity<List<TaskDTO>> searchTasks(
      @Parameter(description = "Search query") @RequestParam String q) {
    UUID userId = securityContextService.getCurrentUserId();
    List<Task> tasks = taskService.searchTasks(userId, q);
    return ResponseEntity.ok(tasks.stream().map(TaskDTO::fromEntity).toList());
  }

  @Operation(
      summary = "Get overdue tasks",
      description = "Returns all overdue tasks for the user")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Overdue tasks retrieved"),
          @ApiResponse(
              responseCode = "401",
              description = "User not authenticated",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/tasks/overdue")
  public ResponseEntity<List<TaskDTO>> getOverdueTasks() {
    UUID userId = securityContextService.getCurrentUserId();
    List<Task> tasks = taskService.getOverdueTasks(userId);
    return ResponseEntity.ok(tasks.stream().map(TaskDTO::fromEntity).toList());
  }

  @Operation(
      summary = "Get due-soon tasks",
      description = "Returns tasks due within the next few days")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Due-soon tasks retrieved"),
          @ApiResponse(
              responseCode = "401",
              description = "User not authenticated",
              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/tasks/due-soon")
  public ResponseEntity<List<TaskDTO>> getDueSoonTasks() {
    UUID userId = securityContextService.getCurrentUserId();
    List<Task> tasks = taskService.getDueSoonTasks(userId);
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
  @GetMapping("/lists/{listId}/tasks")
  public ResponseEntity<List<TaskDTO>> getTasksByList(
      @Parameter(description = "List ID") @PathVariable UUID listId,
      @Parameter(description = "Filter by status") @RequestParam(required = false)
          TaskStatus status,
      @Parameter(description = "Filter by priority") @RequestParam(required = false)
          TaskPriority priority,
      @Parameter(description = "Filter by due date (overdue, today, next_7_days, all)")
      @RequestParam(required = false) String dueDateFilter) {
    UUID userId = securityContextService.getCurrentUserId();
    List<Task> tasks = taskService.getTasksByList(listId, userId);

    DueDateFilter dateFilter = DueDateFilter.fromValue(dueDateFilter);
    tasks = filterByDueDate(tasks, dateFilter);

    if (status != null && priority != null) {
      tasks = tasks.stream()
          .filter(t -> t.getStatus() == status && t.getPriority() == priority)
          .toList();
    } else if (status != null) {
      tasks = tasks.stream()
          .filter(t -> t.getStatus() == status)
          .toList();
    } else if (priority != null) {
      tasks = tasks.stream()
          .filter(t -> t.getPriority() == priority)
          .toList();
    }

    return ResponseEntity.ok(tasks.stream().map(TaskDTO::fromEntity).toList());
  }

  private List<Task> filterByDueDate(List<Task> tasks, DueDateFilter dateFilter) {
    if (dateFilter == null || dateFilter == DueDateFilter.ALL) {
      return tasks;
    }

    java.time.LocalDate today = java.time.LocalDate.now();
    return switch (dateFilter) {
        case OVERDUE -> tasks.stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today)
                && t.getStatus() != TaskStatus.DONE)
            .toList();
        case TODAY -> tasks.stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today)
                && t.getStatus() != TaskStatus.DONE)
            .toList();
        case NEXT_7_DAYS -> tasks.stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today)
                && t.getDueDate().isBefore(today.plusDays(8))
                && t.getStatus() != TaskStatus.DONE)
            .toList();
        default -> tasks;
    };
  }

  @Operation(
      summary = "Create new task",
      description = "Create a new task in the specified list")
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
  @PostMapping("/lists/{listId}/tasks")
  public ResponseEntity<TaskDTO> createTask(
      @Parameter(description = "List ID") @PathVariable UUID listId,
      @Valid @RequestBody CreateTaskRequest request) {
    UUID userId = securityContextService.getCurrentUserId();
    Task task = taskService.createTask(userId, listId, request);
    return ResponseEntity
        .created(URI.create("/api/lists/" + listId + "/tasks/" + task.getId()))
        .body(TaskDTO.fromEntity(task));
  }

  @Operation(
      summary = "Get single task in list",
      description = "Returns details of a specific task in a list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this list or task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task or list not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/lists/{listId}/tasks/{taskId}")
  public ResponseEntity<TaskDTO> getTaskInList(
      @Parameter(description = "List ID") @PathVariable UUID listId,
      @Parameter(description = "Task ID") @PathVariable UUID taskId) {
    UUID userId = securityContextService.getCurrentUserId();
    Task task = taskService.getTaskInList(taskId, listId, userId);
    return ResponseEntity.ok(TaskDTO.fromEntity(task));
  }

  @Operation(
      summary = "Update task in list",
      description = "Update details of a task in a specific list")
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
          description = "User does not own this list or task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task or list not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/lists/{listId}/tasks/{taskId}")
  public ResponseEntity<TaskDTO> updateTaskInList(
      @Parameter(description = "List ID") @PathVariable UUID listId,
      @Parameter(description = "Task ID") @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskRequest request) {
    UUID userId = securityContextService.getCurrentUserId();
    taskService.getTaskInList(taskId, listId, userId);
    Task task = taskService.updateTask(taskId, userId, request);
    return ResponseEntity.ok(TaskDTO.fromEntity(task));
  }

  @Operation(summary = "Delete task in list", description = "Delete a task from a specific list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "User does not own this list or task",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "Task or list not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/lists/{listId}/tasks/{taskId}")
  public ResponseEntity<Void> deleteTaskInList(
      @Parameter(description = "List ID") @PathVariable UUID listId,
      @Parameter(description = "Task ID") @PathVariable UUID taskId) {
    UUID userId = securityContextService.getCurrentUserId();
    taskService.getTaskInList(taskId, listId, userId);
    taskService.deleteTask(taskId, userId);
    return ResponseEntity.noContent().build();
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
  @PatchMapping("/tasks/{taskId}/status")
  public ResponseEntity<TaskDTO> updateTaskStatus(
      @Parameter(description = "Task ID") @PathVariable UUID taskId,
      @Parameter(description = "New status") @RequestParam TaskStatus status) {
    UUID userId = securityContextService.getCurrentUserId();
    Task task = taskService.updateTaskStatus(taskId, userId, status);
    return ResponseEntity.ok(TaskDTO.fromEntity(task));
  }
}
