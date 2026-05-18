package com.taskmanager.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

  private final String resourceName;
  private final Object id;

  public ResourceNotFoundException(String resourceName, UUID id) {
    super(String.format("%s with id %s not found", resourceName, id));
    this.resourceName = resourceName;
    this.id = id;
  }

  public ResourceNotFoundException(String resourceName, String id) {
    super(String.format("%s with id %s not found", resourceName, id));
    this.resourceName = resourceName;
    this.id = id;
  }

  public String getResourceName() {
    return resourceName;
  }

  public Object getId() {
    return id;
  }
}
