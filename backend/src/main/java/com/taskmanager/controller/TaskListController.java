package com.taskmanager.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.taskmanager.dto.CreateTaskListRequest;
import com.taskmanager.dto.ErrorResponse;
import com.taskmanager.dto.TaskListDTO;
import com.taskmanager.dto.UpdateTaskListRequest;
import com.taskmanager.service.SecurityContextService;
import com.taskmanager.service.TaskListService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
@Tag(name = "Lists", description = "Task list/project CRUD operations")
public class TaskListController {
  private final TaskListService taskListService;
  private final SecurityContextService securityContextService;

  @Operation(
      summary = "Get all lists for current user",
      description = "Returns all task lists created by the authenticated user"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Lists retrieved successfully",
          content = @Content(schema = @Schema(implementation = TaskListDTO.class))),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  public ResponseEntity<List<TaskListDTO>> getAllLists() {
    UUID userId = securityContextService.getCurrentUserId();
    List<TaskListDTO> lists = taskListService.getListsByUser(userId);
    return ResponseEntity.ok(lists);
  }

  @Operation(
      summary = "Get task list by ID",
      description = "Returns a specific task list for the authenticated user"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "List retrieved successfully",
          content = @Content(schema = @Schema(implementation = TaskListDTO.class))),
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
  @GetMapping("/{id}")
  public ResponseEntity<TaskListDTO> getList(
      @Parameter(description = "List ID") @PathVariable UUID id
  ) {
    UUID userId = securityContextService.getCurrentUserId();
    TaskListDTO list = taskListService.getListById(id, userId);
    return ResponseEntity.ok(list);
  }

  @Operation(
      summary = "Create new task list",
      description = "Creates a new task list for the authenticated user"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "List created successfully",
          content = @Content(schema = @Schema(implementation = TaskListDTO.class))),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid request",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "401",
          description = "User not authenticated",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<TaskListDTO> createList(
      @Valid @RequestBody CreateTaskListRequest request
  ) {
    UUID userId = securityContextService.getCurrentUserId();
    TaskListDTO created = taskListService.createList(userId, request.getTitle());
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.getId())
        .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @Operation(
      summary = "Update task list",
      description = "Updates the title of an existing task list"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "List updated successfully",
          content = @Content(schema = @Schema(implementation = TaskListDTO.class))),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid request",
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
  @PreAuthorize("@securityService.userOwnsTaskList(#id)")
  @PutMapping("/{id}")
  public ResponseEntity<TaskListDTO> updateList(
      @Parameter(description = "List ID") @PathVariable UUID id,
      @Valid @RequestBody UpdateTaskListRequest request
  ) {
    UUID userId = securityContextService.getCurrentUserId();
    TaskListDTO updated = taskListService.updateList(id, userId, request.getTitle());
    return ResponseEntity.ok(updated);
  }

  @Operation(
      summary = "Delete task list",
      description = "Deletes a task list. Returns 409 if list contains tasks."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "List deleted successfully"),
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
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "409",
          description = "Cannot delete list with tasks",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("@securityService.userOwnsTaskList(#id)")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteList(
      @Parameter(description = "List ID") @PathVariable UUID id
  ) {
    UUID userId = securityContextService.getCurrentUserId();
    taskListService.deleteList(id, userId);
    return ResponseEntity.noContent().build();
  }
}
