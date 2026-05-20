# Phase 5 - Task 3: Enum Validation Implementation Plan

**Task**: Audit Enum Validation in REST Calls and Database Storage  
**Priority**: Medium  
**Status**: 🔄 IN PROGRESS  
**Target**: Complete comprehensive enum validation across request validation, database storage, and error handling

---

## Current State Analysis

### Existing Enums
1. **TaskStatus** (`entity/TaskStatus.java`)
   - Values: TO_DO, IN_PROGRESS, DONE
   - Database: Stored as VARCHAR(50) in task table

2. **TaskPriority** (`entity/TaskPriority.java`)
   - Values: LOW, MEDIUM, HIGH
   - Database: Stored as VARCHAR(50) in task table

3. **DueDateFilter** (`entity/DueDateFilter.java`) ✅ CREATED
   - Values: OVERDUE, TODAY, NEXT_7_DAYS, ALL
   - Type: Query filter (not persisted)
   - Features: Custom `fromValue()` converter, defaults to ALL on invalid input
   - Usage: Task filtering in controllers (`/api/tasks`, `/api/lists/{listId}/tasks`)

### Current Issues & Gaps

| Issue | Impact | Status |
|-------|--------|--------|
| No @Valid annotation on enum fields in DTOs | Invalid status/priority accepted at REST layer | ❌ Missing |
| No custom validation exception for invalid enums | Generic error responses | ❌ Missing |
| No @JsonFormat annotation for enum serialization | API inconsistency in request/response | ⚠️ Partial |
| DueDateFilter lacks validation annotation | Filter validation not enforced | ⚠️ Partial |
| Enum storage not documented in DB schema | Schema mismatch risk | ⚠️ Partial |
| No error handler for enum conversion failures | 500 errors instead of 400 | ❌ Missing |
| No unit tests for enum edge cases | Missing coverage | ❌ Missing |

---

## Implementation Plan

### Phase 1: Add Validation Annotations & Error Handling

#### 1.1 Add @JsonFormat to Enums
**Files to modify**:
- `entity/TaskStatus.java` — Add @JsonFormat to control serialization
- `entity/TaskPriority.java` — Add @JsonFormat to control serialization

**Changes**:
```java
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TaskStatus {
  TO_DO,
  IN_PROGRESS,
  DONE;
}
```
- Ensures JSON serialization always uses enum name (not ordinal)
- Client receives `"status": "to_do"` (snake_case in JSON, not `"to_do"`)

#### 1.2 Update DTOs with @Valid Annotations
**Files to modify**:
- `dto/CreateTaskRequest.java`
- `dto/UpdateTaskRequest.java`

**Changes**: 
- Add field-level validation for status/priority fields
- Example:
```java
@Valid
private TaskStatus status;

@Valid
private TaskPriority priority;
```
- Note: @Valid alone won't validate enums; rely on Spring's built-in enum deserialization error handling

#### 1.3 Add Global Enum Error Handler
**New file**: `exception/EnumDeserializationExceptionHandler.java`

**Responsibility**:
- Catch `HttpMessageNotReadableException` (invalid enum in JSON request)
- Parse the invalid enum value from exception
- Return structured error response (400 Bad Request)
- Example response:
```json
{
  "error": "Invalid enum value",
  "field": "status",
  "value": "invalid_status",
  "allowed_values": ["TO_DO", "IN_PROGRESS", "DONE"],
  "timestamp": "2026-05-20T10:30:00Z"
}
```

**Implementation**:
- Extend `ResponseEntityExceptionHandler` or add `@ExceptionHandler` to existing `ControllerAdvice`
- Map `HttpMessageNotReadableException` to structured error response

---

### Phase 2: Enhance DueDateFilter with @JsonCreator

#### 2.1 Add @JsonCreator to DueDateFilter
**File**: `entity/DueDateFilter.java`

**Responsibility**:
- Use `@JsonCreator` static method to handle deserialization
- Throws `IllegalArgumentException` on invalid filter value
- Jackson automatically calls this method on deserialization
- Example:
```java
@JsonCreator
public static DueDateFilter fromValue(String value) {
  if (value == null || value.isEmpty()) {
    return ALL;  // Default for null/empty query param
  }
  for (DueDateFilter filter : DueDateFilter.values()) {
    if (filter.value.equals(value.toLowerCase())) {
      return filter;
    }
  }
  // Throw exception for invalid values → caught by global error handler → 400 response
  throw new IllegalArgumentException(
    String.format("Invalid due date filter: '%s'. Allowed: overdue, today, next_7_days, all", value)
  );
}
```

**Key difference**:
- Single `fromValue()` method with `@JsonCreator` 
- Handles both deserialization (JSON) and direct calls
- Throws exception on invalid input (no silent fallback)
- Controller query parameters default to null → method returns ALL

---

### Phase 3: Comprehensive Testing

#### 3.1 Unit Tests for Enums
**New file**: `src/test/java/com/taskmanager/entity/EnumValidationTest.java`

**Test cases**:
- ✅ Valid enum values deserialize correctly
- ✅ Invalid enum values throw exception (HttpMessageNotReadableException)
- ✅ Null values handled appropriately
- ✅ Case sensitivity respected (only exact match)

#### 3.2 Controller Integration Tests
**File**: `src/test/java/com/taskmanager/controller/TaskControllerEnumValidationTest.java`

**Test cases**:
- ✅ POST /api/lists/{id}/tasks with invalid status → 400 Bad Request
- ✅ POST /api/lists/{id}/tasks with invalid priority → 400 Bad Request
- ✅ PUT /api/lists/{id}/tasks/{id} with invalid status → 400 Bad Request
- ✅ GET /api/tasks?dueDateFilter=invalid → 400 Bad Request (or default to ALL per business logic)
- ✅ Response includes allowed enum values in error message

#### 3.3 Edge Case Tests
- ✅ Empty string for enum field
- ✅ Whitespace in enum value
- ✅ Unicode/special characters
- ✅ Numeric string values
- ✅ Case variations (to_do vs TO_DO vs To_Do)

---

## Implementation Tasks (Ordered)

1. **Add @JsonFormat to TaskStatus & TaskPriority enums**
   - Ensures consistent JSON serialization format
   - Time estimate: 15 min

2. **Add @JsonCreator to DueDateFilter enum**
   - Single `fromValue()` method handles deserialization with validation
   - Throws IllegalArgumentException on invalid input
   - Time estimate: 15 min

3. **Create global enum error handler**
   - Catch HttpMessageNotReadableException & IllegalArgumentException
   - Return structured 400 error with allowed values
   - Time estimate: 45 min

4. **Write comprehensive unit tests**
   - Enum validation test suite (valid/invalid values)
   - Time estimate: 45 min

5. **Write controller integration tests**
   - Invalid enum rejection tests
   - Error response validation
   - Time estimate: 60 min

6. **Run full test suite & verify build**
   - `gradle build` — ensure all tests pass
   - `gradle checkstyleMain` — verify code style
   - Time estimate: 10 min

---

## Enum Summary Table

| Enum | Values | Persisted | Validation | Error Handling |
|------|--------|-----------|-----------|-----------------|
| **TaskStatus** | TO_DO, IN_PROGRESS, DONE | ✅ Yes (task.status) | ⚠️ Partial | ❌ Missing |
| **TaskPriority** | LOW, MEDIUM, HIGH | ✅ Yes (task.priority) | ⚠️ Partial | ❌ Missing |
| **DueDateFilter** | OVERDUE, TODAY, NEXT_7_DAYS, ALL | ❌ No (query filter) | ⚠️ Partial | ⚠️ Partial |

---

## Acceptance Criteria

- ✅ Invalid enum values in POST/PUT requests return 400 (not 500)
- ✅ Error response includes allowed values list
- ✅ 100% of enum deserialization edge cases covered by tests
- ✅ Controller tests verify 400 response for invalid enums
- ✅ `gradle build` passes without errors
- ✅ `gradle checkstyleMain` passes
- ✅ Code coverage for enum validation logic ≥ 80%

---

## Success Metrics

1. **API Error Clarity**: Users receive clear error messages with allowed values
2. **Data Integrity**: Database enforces enum constraints at schema level
3. **Test Coverage**: All enum edge cases tested and passing
4. **Code Quality**: Consistent enum handling across all layers (REST → Service → Database)

---

## Notes

- DueDateFilter is **query-only** (not persisted) — validation should be lenient (default to ALL on invalid)
- TaskStatus & TaskPriority **are persisted** — validation must be strict (reject invalid)
- Keep both `fromValue()` (lenient) and `fromValueStrict()` (strict) methods for backward compatibility
- Update README.md with allowed enum values documentation

