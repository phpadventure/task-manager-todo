package com.taskmanager.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

  private UUID id;
  private UUID listId;
  private UUID userId;
  private String title;
  private String description;
  private TaskStatus status;
  private TaskPriority priority;
  private LocalDate dueDate;
  private Instant createdAt;
  private Instant updatedAt;

  public static TaskDTO fromEntity(Task task) {
    return new TaskDTO(
        task.getId(),
        task.getListId(),
        task.getUserId(),
        task.getTitle(),
        task.getDescription(),
        task.getStatus(),
        task.getPriority(),
        task.getDueDate(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}
