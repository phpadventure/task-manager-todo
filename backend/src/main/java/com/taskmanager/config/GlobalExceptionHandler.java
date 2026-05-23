package com.taskmanager.config;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.taskmanager.dto.ErrorResponse;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.exception.InvalidEnumException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    LOG.warn("Resource not found: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(ex.getMessage(), Instant.now().toString(), HttpStatus.NOT_FOUND.value());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
    LOG.warn("Unauthorized access: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            ex.getMessage(), Instant.now().toString(), HttpStatus.FORBIDDEN.value());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    LOG.warn("Access denied: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            "Access denied", Instant.now().toString(), HttpStatus.FORBIDDEN.value());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
    LOG.warn("Validation error: {}", ex.getMessage());
    Map<String, String> details = null;
    if (ex.getFieldName() != null) {
      details = new HashMap<>();
      details.put(ex.getFieldName(), ex.getValidationRule());
    }
    ErrorResponse response =
        new ErrorResponse(
            "Validation failed", details, Instant.now().toString(), HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
    LOG.warn("Conflict: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            ex.getMessage(), Instant.now().toString(), HttpStatus.CONFLICT.value());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(InvalidEnumException.class)
  public ResponseEntity<ErrorResponse> handleInvalidEnumException(InvalidEnumException ex) {
    LOG.warn("Invalid enum value: {}", ex.getMessage());
    Map<String, String> details = new HashMap<>();
    String msg = "Invalid value: " + ex.getInvalidValue() + ". Allowed: "
        + ex.getAllowedValues();
    details.put(ex.getFieldName(), msg);
    ErrorResponse response =
        new ErrorResponse(
            "Invalid enum value", details, Instant.now().toString(),
            HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    LOG.warn("Invalid request format: {}", ex.getMessage());
    String message = "Invalid request format";
    Map<String, String> details = null;

    String exMessage = ex.getMessage();
    if (exMessage != null) {
      if (exMessage.contains("java.time.LocalDate")) {
        message = "Validation failed";
        details = new HashMap<>();
        String fieldName = extractFieldNameFromMessage(exMessage);
        details.put(fieldName != null ? fieldName : "dueDate",
            "Invalid date format. Expected format: yyyy-MM-dd");
      } else if (exMessage.contains("not one of the values accepted for Enum")) {
        message = "Validation failed";
        details = new HashMap<>();
        String fieldName = extractFieldNameFromMessage(exMessage);
        String allowed = extractAllowedEnumValues(exMessage);
        if (allowed != null) {
          details.put(fieldName != null ? fieldName : "field",
              "Invalid value. Allowed: " + allowed);
        }
      }
    }

    ErrorResponse response =
        new ErrorResponse(
            message, details, Instant.now().toString(), HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  private String extractFieldNameFromMessage(String message) {
    int idx = message.indexOf("from Object [\"");
    if (idx >= 0) {
      int endIdx = message.indexOf("\"]", idx);
      if (endIdx > idx) {
        return message.substring(idx + 14, endIdx);
      }
    }
    return null;
  }

  private String extractAllowedEnumValues(String message) {
    int startIdx = message.lastIndexOf('[');
    int endIdx = message.lastIndexOf(']');
    if (startIdx >= 0 && endIdx > startIdx) {
      return message.substring(startIdx, endIdx + 1);
    }
    return null;
  }



  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    LOG.warn("Invalid argument: {}", ex.getMessage());
    ErrorResponse response =
        new ErrorResponse(
            ex.getMessage(), Instant.now().toString(), HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    LOG.warn("Validation error: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    ErrorResponse response =
        new ErrorResponse(
            "Validation failed", errors, Instant.now().toString(), HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
    LOG.warn("Resource not found: {}", ex.getResourcePath());
    ErrorResponse response =
        new ErrorResponse("Not found", Instant.now().toString(), HttpStatus.NOT_FOUND.value());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    LOG.error("Unexpected error", ex);
    ErrorResponse response =
        new ErrorResponse(
            "Internal server error",
            Instant.now().toString(),
            HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
