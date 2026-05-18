package com.taskmanager.exception;

public class UnauthorizedException extends RuntimeException {

  private final String resourceType;

  public UnauthorizedException(String message) {
    super(message);
    this.resourceType = null;
  }

  public UnauthorizedException(String message, String resourceType) {
    super(message);
    this.resourceType = resourceType;
  }

  public String getResourceType() {
    return resourceType;
  }
}
