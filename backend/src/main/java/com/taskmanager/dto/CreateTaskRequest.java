package com.taskmanager.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.util.ValidationMessages;
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

  @NotBlank(message = ValidationMessages.TITLE_REQUIRED)
  private String title;

  private String description;
  private TaskStatus status;
  private TaskPriority priority;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = ValidationMessages.DATE_FORMAT_PATTERN)
  private LocalDate dueDate;
}
