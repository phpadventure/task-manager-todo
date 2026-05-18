package com.taskmanager.exception;

public class ConflictException extends RuntimeException {

  private final String conflictReason;

  public ConflictException(String message) {
    super(message);
    this.conflictReason = null;
  }

  public ConflictException(String message, String conflictReason) {
    super(message);
    this.conflictReason = conflictReason;
  }

  public String getConflictReason() {
    return conflictReason;
  }
}
