package com.taskmanager.dto;

import java.time.LocalDate;

import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

  @NotBlank(message = "Title is required")
  private String title;

  private String description;
  private TaskStatus status;
  private TaskPriority priority;
  private LocalDate dueDate;
}
