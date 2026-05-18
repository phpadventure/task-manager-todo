package com.taskmanager.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private String error;
  private Map<String, String> details;
  private String timestamp;
  private Integer status;

  public ErrorResponse() {}

  public ErrorResponse(String error, String timestamp, Integer status) {
    this.error = error;
    this.timestamp = timestamp;
    this.status = status;
  }

  public ErrorResponse(
      String error, Map<String, String> details, String timestamp, Integer status) {
    this.error = error;
    this.details = details;
    this.timestamp = timestamp;
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Map<String, String> getDetails() {
    return details;
  }

  public void setDetails(Map<String, String> details) {
    this.details = details;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}
