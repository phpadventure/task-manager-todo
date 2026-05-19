# Backend CLAUDE.md

Backend guidelines for the Personal To-Do Manager API. Follow these conventions for consistency across all layers.

---

## Flyway Migrations

**Location**: `src/main/resources/db/migration/`

**Naming pattern**: `V{version}__Description.sql`
- Example: `V001__create_users_table.sql`
- `{version}` = 001, 002, 003... sequential version number
- `__` (double underscore) = Flyway separator
- Description in snake_case (no spaces)

**Rules**:
- **Idempotency**: Always use `CREATE TABLE IF NOT EXISTS`, `ALTER TABLE IF EXISTS`
- **Single responsibility**: One logical change per migration (don't mix schema + seed data)
- **No downtime**: Avoid `DROP COLUMN`—mark unused first, remove in later migration
- **Rollback safety**: Every migration must be reversible (test with manual rollback in dev)
- **Comments**: Add SQL comments explaining non-obvious changes
- **Order matters**: Flyway runs in lexicographic order; date-based names ensure correct sequence
- **No cascade**: Do NOT use `ON DELETE CASCADE` or `ON UPDATE CASCADE`. Handle deletion logic in application layer.

---

## Java Patterns & Layers

### **Project Structure**

```
src/main/java/com/taskmanager/
├── controller/          # REST endpoints
├── service/             # Business logic
├── repository/          # JPA repositories
├── entity/              # JPA entities (database models)
├── dto/
│   ├── request/         # CreateTaskRequest, UpdateTaskRequest, etc.
│   └── response/        # TaskResponse, UserResponse, etc.
├── config/              # Spring configuration (Security, WebSocket, OpenAPI)
├── security/            # OAuth2UserService, SecurityUser
├── websocket/           # WebSocket handlers & schedulers
├── exception/           # Custom exceptions
└── util/                # Utility classes, mappers
```

### **Naming Conventions**

- **Endpoints (REST URLs)**: kebab-case
  - ✅ `GET /api/my-tasks`, `POST /api/task-lists`
  - ❌ `GET /api/myTasks` or `/api/my_tasks`

- **JSON response fields**: snake_case
  - ✅ `{ "user_id": "550e8400-e29b-41d4-a716-446655440000", "created_at": "2026-05-17T..." }`
  - ❌ `{ "userId": "...", "createdAt": "..." }`
  - Configure in `application.properties`:
    ```properties
    spring.jackson.property-naming-strategy=SNAKE_CASE
    ```

- **Java variables**: camelCase
  - ✅ `String userName;`, `int taskCount;`
  - ❌ `String user_name;`

- **Java class names**: PascalCase
  - ✅ `class UserRepository`, `class TaskService`, `class TaskController`
  - ❌ `class userRepository` or `class user_repository`

### **ID Fields (UUID Strategy)**

- **Type**: UUID (36 chars, hyphenated format)
  - ✅ `550e8400-e29b-41d4-a716-446655440000`
  - ❌ `Integer id;` or `String id;` (without UUID)

- **Generation**: Application-generated (server-side in Java entity, not database or client)
  - Use `@GeneratedValue(strategy = GenerationType.UUID)` in JPA entity
  - Immutable once set; treated as generated value
  - Always returned in response DTOs

- **Java declaration**:
  ```java
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "CHAR(36)")
  @JdbcTypeCode(Types.VARCHAR)
  private UUID id;
  ```

  ALSO DO NOT FORGET ADD `@JdbcTypeCode(Types.VARCHAR)` TO ALL PLACE WHERE IN DB IS CHAR(36) BUT ENTITY FIELD IS UUID

- **JSON serialization**: Automatically serialized as string by Jackson
  - ✅ `"id": "550e8400-e29b-41d4-a716-446655440000"`

### **Application Layers**

1. **Controllers**: REST endpoints, request validation, status codes, no business logic
   - Use kebab-case for route paths
   - Return response DTOs (not entities)
   - Document all endpoints with OpenAPI annotations (`@Operation`, `@Parameter`, `@RequestBody`)

2. **Services**: Business logic, authorization checks (`@Secured`), transaction boundaries (`@Transactional`)
   - Verify user owns the resource before operations
   - Throw custom exceptions (caught by `@ControllerAdvice`)
   - Handle deletion logic on application side (check for dependencies, prevent invalid states)

3. **Repositories**: Entity queries, use Spring Data JPA naming conventions
   - **Simple queries**: Use method naming (`findByProjectIdAndUserId()`)
   - **Complex queries**: Use `@Query(nativeQuery=false)` with JPQL/HQL first
     ```java
     @Query("SELECT t FROM Task t WHERE t.userId = ?1 AND t.status = ?2")
     List<Task> findTasksByUserAndStatus(UUID userId, String status);
     ```
   - **Very complex queries**: Use Specifications for dynamic/reusable filters
     ```java
     Specification<Task> spec = TaskSpecifications.hasUserId(userId)
         .and(TaskSpecifications.hasStatus("DONE"));
     repository.findAll(spec);
     ```

4. **DTOs**: Separate request/response models; use `@Valid` for validation
   - **Request**: `CreateTaskRequest`, `UpdateTaskRequest` (in `dto/request/`)
   - **Response**: `TaskResponse`, `UserResponse` (in `dto/response/`)
     - Always include `id` as UUID string
     - Include `created_at`, `updated_at` timestamps
   - Use `@JsonProperty` only if name differs from snake_case default

5. **Entities**: Database models, use `@Entity`, `@Table`, no business logic
   - Kept in `entity/` folder, separate from DTOs
   - Use UUID for all ID fields (strategy: `GenerationType.UUID`)

6. **Exceptions**: Custom `NotFoundException`, `UnauthorizedException`, etc.; map to HTTP 404, 403

---

## MySQL Patterns

### **Schema Design & Naming**

- **Table names**: singular, snake_case
  - ✅ `user`, `task`, `task_list`
  - ❌ `users`, `tasks`, `my_tasks_table`

- **Column names**: snake_case
  - ✅ `user_id`, `created_at`, `is_active`
  - ❌ `userId`, `createdAt`, `isActive`

- **ID columns**: CHAR(36) for UUID
  - ✅ `id CHAR(36) PRIMARY KEY`
  - ❌ `id INT AUTO_INCREMENT` or `id VARCHAR(255)`
  - MySQL stores UUID efficiently; CHAR(36) for readability and consistency

- **Foreign key columns**: `{table}_id` format
  - ✅ `user_id CHAR(36) NOT NULL, FOREIGN KEY (user_id) REFERENCES user(id)`
  - ❌ `user_fk` or just `user`
  - Do NOT use `ON DELETE CASCADE` or `ON UPDATE CASCADE` — handle in application

- **Timestamps**: `created_at`, `updated_at` (both UTC, MySQL TIMESTAMP or DATETIME)
  - ✅ `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`
  - ✅ `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
  - ❌ `created` or `createdDate`

- **Boolean fields**: `is_` prefix, TINYINT(1)
  - ✅ `is_active TINYINT(1) DEFAULT 1`
  - ❌ `active` or `bool_active`

### **Constraints & Indexes**

- **NOT NULL** on all required fields (enforce at schema level)
- **UNIQUE** on email (for user uniqueness)
- **COMPOSITE UNIQUE** on (`provider`, `provider_user_id`) for OAuth providers
- **COMPOSITE UNIQUE** on (`user_id`, `provider`) to prevent duplicate providers per user
- **Indexes** on:
  - FK columns (for join performance)
  - Search fields (`title`, `description`)
  - Filter fields (`status`, `priority`)
  - Lookup fields (`email`)

Example:
```sql
CREATE TABLE user (
  id CHAR(36) PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  display_name VARCHAR(255),
  avatar_url VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_auth_provider (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  provider VARCHAR(50) NOT NULL,
  provider_user_id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id),
  UNIQUE KEY unique_provider_user (provider, provider_user_id),
  UNIQUE KEY unique_user_provider (user_id, provider),
  INDEX idx_user_id (user_id)
);

CREATE TABLE task (
  id CHAR(36) PRIMARY KEY,
  list_id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status VARCHAR(50) NOT NULL DEFAULT 'TO_DO',
  priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
  due_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (list_id) REFERENCES task_list(id),
  FOREIGN KEY (user_id) REFERENCES user(id),
  INDEX idx_user_id (user_id),
  INDEX idx_list_id (list_id),
  INDEX idx_status (status),
  INDEX idx_priority (priority),
  INDEX idx_due_date (due_date)
);
```

---

## API Documentation (OpenAPI / Swagger)

### **Configuration**

- Use SpringDoc OpenAPI (springdoc-openapi-starter-webmvc-ui) for OpenAPI 3.0 integration
- Add to `build.gradle`:
  ```gradle
  implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'
  ```

- Configure in `application.properties`:
  ```properties
  springdoc.api-docs.path=/api-docs
  springdoc.swagger-ui.path=/swagger-ui.html
  springdoc.swagger-ui.operationsSorter=method
  ```

### **Annotation Guidelines**

Document all endpoints with OpenAPI annotations:

```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
  
  @Operation(summary = "Get all tasks for user", 
             description = "Returns paginated list of tasks with optional filters")
  @GetMapping
  public ResponseEntity<List<TaskResponse>> getTasks(
    @Parameter(description = "Filter by task status")
    @RequestParam(required = false) String status,
    
    @Parameter(description = "Filter by priority (LOW, MEDIUM, HIGH)")
    @RequestParam(required = false) String priority
  ) {
    // implementation
  }

  @Operation(summary = "Create new task")
  @PostMapping
  public ResponseEntity<TaskResponse> createTask(
    @RequestBody @Valid CreateTaskRequest request
  ) {
    // implementation
  }
}
```

- Use `@Operation` on all controller methods
- Use `@Parameter` for path/query/header parameters
- Use `@RequestBody` with `@Valid` for request bodies
- Document response types with `@io.swagger.v3.oas.annotations.responses.ApiResponse`

---

## API Response Structure

Use resource-centric response format with standard HTTP status codes:

### **Success Response (2xx)**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "status": "to_do",
  "priority": "high",
  "due_date": "2026-05-20",
  "user_id": "550e8400-e29b-41d4-a716-446655440001",
  "list_id": "550e8400-e29b-41d4-a716-446655440002",
  "created_at": "2026-05-17T10:30:00Z",
  "updated_at": "2026-05-17T10:30:00Z"
}
```

### **List Response (array)**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Buy groceries",
    "status": "to_do",
    "priority": "high",
    "due_date": "2026-05-20",
    "created_at": "2026-05-17T10:30:00Z"
  }
]
```

### **Error Response (4xx, 5xx)**
```json
{
  "error": "Resource not found",
  "details": {
    "field": "task_id",
    "message": "Task with id 550e8400... does not exist"
  },
  "timestamp": "2026-05-17T10:30:00Z"
}
```

**HTTP Status Codes**:
- `200 OK` — GET, retrieve success
- `201 Created` — POST, resource created (include `Location` header)
- `204 No Content` — DELETE success (no body)
- `400 Bad Request` — Validation error
- `401 Unauthorized` — Not authenticated
- `403 Forbidden` — Authenticated but no access (user doesn't own resource)
- `404 Not Found` — Resource doesn't exist
- `409 Conflict` — Business logic violation (e.g., delete non-empty list)
- `500 Internal Server Error` — Server error

---

## Test Rules

### **Test Strategy**

- **Controller Tests**: Verify HTTP endpoints work correctly (request/response mapping, status codes)
  - Location: `src/test/java/com/taskmanager/controller/`
  - Use `@WebMvcTest` for isolated controller testing
  - Mock service layer dependencies
  - Focus on: HTTP status codes, response format, error handling

- **Service Tests**: Business logic in isolation
  - Location: `src/test/java/com/taskmanager/service/`
  - Use `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`
  - Mock repository layer
  - Focus on: business rules, authorization, edge cases

- **Authorization Tests**: Every endpoint must be tested for 403 when user doesn't own resource
  - Example: User A tries to access User B's task → 403 Forbidden
  - Verify authorization at both controller and service layers

### **Test Naming Convention**

Pattern: `test{Method}_{Given}_{Should}`

Examples:
- `testGetTask_ValidId_ShouldReturnOk`
- `testGetTask_UserDoesNotOwnResource_ShouldReturnForbidden`
- `testDeleteList_ListExists_ShouldReturnNoContent`
- `testCreateTask_InvalidInput_ShouldReturnBadRequest`

### **Testing Approach**

```java
@WebMvcTest(TaskController.class)
class TaskControllerTest {
  
  @Autowired
  private MockMvc mockMvc;
  
  @MockBean
  private TaskService taskService;
  
  @Test
  void testGetTask_ValidId_ShouldReturnOk() throws Exception {
    UUID taskId = UUID.randomUUID();
    TaskResponse response = new TaskResponse(taskId, "Test task", ...);
    
    when(taskService.getTask(taskId)).thenReturn(response);
    
    mockMvc.perform(get("/api/tasks/{id}", taskId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(taskId.toString()));
  }
}
```

### **Coverage Goals**

- Minimum 70% code coverage (controllers, services, critical business logic)
- 100% coverage for authorization checks
- Focus on: happy path, error cases, boundary conditions

### **Running Tests**

```bash
# All tests
gradle test

# Single test class
gradle test --tests com.taskmanager.controller.TaskControllerTest

# Single test method
gradle test --tests com.taskmanager.controller.TaskControllerTest.testGetTask_ValidId_ShouldReturnOk

# With coverage report (excludes DTOs, Entities, Config, Exceptions, etc.)
gradle jacocoTestReport

# View coverage report (opens HTML in browser)
# macOS/Linux:
open build/reports/jacoco/test/html/index.html

# Watch mode (reruns on changes)
gradle test --continuous

# Check code style (Checkstyle - Google Java Style)
gradle checkstyleMain
```

---

## Authorization & Security

### **User Ownership Verification**

Every endpoint must verify the authenticated user owns the resource:

```java
@GetMapping("/tasks/{id}")
public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
  Task task = taskService.findById(id)
    .orElseThrow(() -> new NotFoundException("Task not found"));
  
  UUID currentUserId = getCurrentUserId(); // from SecurityContext
  if (!task.getUserId().equals(currentUserId)) {
    throw new UnauthorizedException("You don't have access to this task");
  }
  
  return ResponseEntity.ok(taskMapper.toResponse(task));
}
```

Or use `@Secured`/`@PreAuthorize` in service layer for cleaner code.

### **WebSocket Authorization**

- Verify user is authenticated before accepting WebSocket connection
- Filter notifications to only include tasks owned by connected user
- All messages must reference authenticated user's context

### **Deletion Logic (Application Layer)**

- Handle all deletion checks and dependencies in service layer
- Example: Cannot delete list if it has active tasks (block pattern)
  ```java
  public void deleteList(UUID listId) {
    List list = listRepository.findById(listId)
      .orElseThrow(() -> new NotFoundException("List not found"));
    
    if (!list.getUserId().equals(getCurrentUserId())) {
      throw new UnauthorizedException("Cannot delete another user's list");
    }
    
    int taskCount = taskRepository.countByListId(listId);
    if (taskCount > 0) {
      throw new ConflictException("Cannot delete list with active tasks");
    }
    
    listRepository.deleteById(listId);
  }
  ```

---

## Code Quality

- **Checkstyle**: Google Java Style enforced via `gradle checkstyleMain`—run before committing
- **No commented code**: Delete unused code, don't comment it out
- **No `TODO` comments without context**: If needed, include issue number and clear action
- **No multiple return points in long methods**: Prefer early returns for validation
- **Logging**: Use SLF4J, log at appropriate levels (INFO, WARN, ERROR)
- **Exception handling**: Catch checked exceptions, let unchecked bubble up (or handle globally)

---
