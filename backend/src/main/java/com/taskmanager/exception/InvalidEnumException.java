package com.taskmanager.exception;

public class InvalidEnumException extends RuntimeException {

  private final String fieldName;
  private final String invalidValue;
  private final String allowedValues;

  public InvalidEnumException(String fieldName, String invalidValue, String allowedValues) {
    super(String.format("Invalid value '%s' for field '%s'. Allowed values: %s",
        invalidValue, fieldName, allowedValues));
    this.fieldName = fieldName;
    this.invalidValue = invalidValue;
    this.allowedValues = allowedValues;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getInvalidValue() {
    return invalidValue;
  }

  public String getAllowedValues() {
    return allowedValues;
  }
}
