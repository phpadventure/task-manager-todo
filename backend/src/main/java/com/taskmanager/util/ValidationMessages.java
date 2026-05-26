package com.taskmanager.util;

public final class ValidationMessages {

  private ValidationMessages() {}

  // Date format pattern and message
  public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
  public static final String DUE_DATE_FORMAT_ERROR =
      "Invalid date format. Expected format: yyyy-MM-dd";

  // Required field messages
  public static final String TITLE_REQUIRED = "Title is required";
  public static final String LIST_NAME_REQUIRED = "List name is required";
  public static final String STATUS_REQUIRED = "Status is required";
  public static final String PRIORITY_REQUIRED = "Priority is required";
  public static final String DUE_DATE_REQUIRED = "Due date is required";
}
