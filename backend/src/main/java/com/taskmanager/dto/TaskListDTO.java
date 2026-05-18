package com.taskmanager.dto;

import java.time.Instant;
import java.util.UUID;

import com.taskmanager.entity.TaskList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskListDTO {
  private UUID id;
  private String title;
  private Instant createdAt;
  private Instant updatedAt;

  public static TaskListDTO fromEntity(TaskList taskList) {
    return new TaskListDTO(
        taskList.getId(),
        taskList.getTitle(),
        taskList.getCreatedAt(),
        taskList.getUpdatedAt()
    );
  }
}
