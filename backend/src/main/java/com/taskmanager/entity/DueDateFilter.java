package com.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum DueDateFilter {
  OVERDUE("overdue"),
  TODAY("today"),
  NEXT_7_DAYS("next_7_days"),
  ALL("all");

  private final String value;

  DueDateFilter(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @JsonCreator
  public static DueDateFilter fromValue(String value) {
    if (value == null || value.isEmpty()) {
      return ALL;
    }
    String lowerValue = value.toLowerCase();
    for (DueDateFilter filter : DueDateFilter.values()) {
      if (filter.value.equals(lowerValue)) {
        return filter;
      }
    }
    throw new IllegalArgumentException(
      String.format("Invalid due date filter: '%s'. Allowed: overdue, today, next_7_days, all", 
        value)
    );
  }
}
