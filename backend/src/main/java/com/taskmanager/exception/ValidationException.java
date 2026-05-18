package com.taskmanager.exception;

public class ValidationException extends RuntimeException {

  private final String fieldName;
  private final String validationRule;

  public ValidationException(String message) {
    super(message);
    this.fieldName = null;
    this.validationRule = null;
  }

  public ValidationException(String fieldName, String validationRule) {
    super(String.format("Validation failed for field '%s': %s", fieldName, validationRule));
    this.fieldName = fieldName;
    this.validationRule = validationRule;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getValidationRule() {
    return validationRule;
  }
}
