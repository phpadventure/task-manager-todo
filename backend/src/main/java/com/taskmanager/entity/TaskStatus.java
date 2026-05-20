package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TaskStatus {
  TO_DO,
  IN_PROGRESS,
  DONE
}
