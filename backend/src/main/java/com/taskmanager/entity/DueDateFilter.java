package com.taskmanager.entity;

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

  public static DueDateFilter fromValue(String value) {
    if (value == null || value.isEmpty()) {
      return ALL;
    }
    for (DueDateFilter filter : DueDateFilter.values()) {
      if (filter.value.equals(value)) {
        return filter;
      }
    }
    return ALL;
  }
}
