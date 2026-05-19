# Backend Code Review Report
**Date**: 2026-05-18  
**Project**: Personal To-Do Manager  
**Backend Size**: ~1,602 lines of Java code

---

## Executive Summary

The backend codebase is **well-structured** with solid fundamentals around authentication, security, and layering. However, there are **critical gaps** preventing the application from being feature-complete:

- ❌ **Missing Task Controller/Service** — No task CRUD operations implemented
- ❌ **Incomplete Repository** — TaskRepository lacks necessary query methods
- ❌ **Missing WebSocket layer** — Real-time notifications not implemented
- ❌ **Limited Testing** — Only TaskList tested, no Task/Auth/User coverage
- ⚠️ **Minor improvements** — Session cookie config, logging edge cases

**Risk Level**: MEDIUM (feature gaps are blocking, but security/architecture is sound)

---

## 1. Architecture & Design ✅

### Strengths
- **Clean separation of concerns**: Controllers → Services → Repositories → Entities
- **Proper use of Spring annotations**: `@Entity`, `@Service`, `@Transactional`, `@RestController`
- **Consistent naming conventions**: Follows backend CLAUDE.md exactly (snake_case JSON, camelCase Java)
- **DTO pattern properly implemented**: Request/Response separation prevents entity leakage
- **Exception hierarchy**: Custom exceptions (`ResourceNotFoundException`, `UnauthorizedException`, `ConflictException`) map cleanly to HTTP status codes
- **Dependency injection**: All services properly injected via `@RequiredArgsConstructor` (Lombok)

### Minor Observations
- No `@Secured` or `@PreAuthorize` annotations used — authorization checks happen in services/controllers (acceptable for MVP)
- Task entity has both `@ManyToOne` and direct UUID columns — not wrong, but redundant (the direct columns are what's used in queries)

---

## 2. Security 🔒

### Strengths
- **OAuth2 properly integrated**: CustomOAuth2UserService correctly extracts provider-specific user attributes (Google's `sub`, GitHub's `id`)
- **Multi-provider support**: Handles Google and GitHub with different attribute mappings
- **User ownership checks**: Services verify user owns resource before returning/modifying (TaskListService::getListById, deleteList)
- **CSRF disabled intentionally**: Appropriate for stateless OAuth2 + session-based approach
- **Session security configured**: `HttpOnly=true`, `SameSite=Lax` in application.properties
- **Exception handler suppresses internal errors**: Generic `Exception` caught and returns "Internal server error" without stack trace exposure

### Issues

#### 1. **CRITICAL: Missing Task Authorization Checks** 🔴
**File**: *No TaskController exists*
**Issue**: The Task entity has a `user_id` field, but there's no controller/service layer to enforce ownership. If a Task controller is added without authorization, cross-user data leakage is possible.
**Example of the risk**:
```java
// Dangerous (if someone adds this):
@GetMapping("/tasks/{id}")
public TaskResponse getTask(@PathVariable UUID id) {
  return taskService.getTask(id); // No userId check!
}
```
**Fix needed**: Ensure all task operations pass `userId` to service and verify ownership.

#### 2. **MEDIUM: Session Cookie Domain Configuration** 🟡
**File**: `application.properties` (line 52), `SecurityConfig.java`
**Issue**: The cookie domain is hardcoded in properties but never actually configured in SecurityConfig. The reverse-proxy setup (documented in `.claude/rules/reverse-proxy-setup.md`) requires proper domain handling for Docker/production.
**Current state**:
```properties
# In application.properties (never read by Spring Security config)
spring.servlet.session.cookie.domain=.localhost
```
**Problem**: Spring Security uses default domain (null = current domain), potentially breaking the multi-origin setup described in CLAUDE.md.
**Fix needed**: Add SessionCookieConfig bean in SecurityConfig to actually apply the domain:
```java
@Bean
public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerCustomizer() {
  return factory -> factory.getSession().getCookie()
    .setDomain(environment.getProperty("server.servlet.session.cookie.domain", "localhost"));
}
```

#### 3. **LOW: AuthController.getCurrentUser() Error Handling** 🟡
**File**: `AuthController.java` (lines 42-55)
**Issue**: Catches `ClassCastException` but does not log it. If this exception occurs, it's silently swallowed.
```java
try {
  CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
  // ...
} catch (ClassCastException e) {
  return ResponseEntity.status(401).build(); // Silent failure
}
```
**Better approach**: Log the error for debugging authentication issues:
```java
LOG.warn("Unexpected principal type during /me request", e);
```

---

## 3. Feature Completeness ❌

### Implemented ✅
- User authentication (OAuth2 Google & GitHub)
- User profile retrieval (`/api/auth/me`)
- Task list CRUD operations (`/api/lists`)
- Basic authorization (list operations check user_id)
- Seed data endpoint (`/api/seed`)
- Health check (`/health`)

### Missing ❌

#### 1. **CRITICAL: Task Controller & Service**
Tasks are the core entity but have **zero API endpoints**. Requirements specify:
- Create task
- Edit task
- Delete task
- Quick status change
- Search by title/description
- Filter by status, priority, due_date

**Impact**: Application is non-functional without this layer.

#### 2. **CRITICAL: Task Repository Methods**
**File**: `TaskRepository.java` (only 12 lines)
**Issue**: Only has `countByListId()`. Missing queries needed for features:
```java
// Currently missing:
List<Task> findByListIdAndUserId(UUID listId, UUID userId);
Task findByIdAndUserId(UUID id, UUID userId);
List<Task> findByUserIdAndStatusAndDueDateBetween(...);
List<Task> findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContaining(...);
int countByListId(UUID listId); // ✅ exists
```

#### 3. **CRITICAL: WebSocket Implementation**
No WebSocket handlers, schedulers, or configuration found. Requirements specify:
- Real-time overdue task notifications
- Real-time due-soon notifications (next 3 days)
- Subscribe/ack message handling
- Periodic checks while client connected

**File missing**: No `websocket/` package exists.

#### 4. **MEDIUM: Seed Data Endpoint**
**File**: `TaskListController.java` references `/api/seed` in SecurityConfig but endpoint is not implemented.
```java
.requestMatchers("/api/seed").permitAll()
```
Should provide sample data for manual testing.

---

## 4. Data Layer 🗄️

### Database Schema ✅ Well-Designed
- UUID primary keys across all tables
- Proper composite unique constraints (provider + provider_user_id)
- Helpful indexes on foreign keys and query columns (status, priority, due_date)
- FULLTEXT index on task title/description (supports search requirement)
- Foreign keys properly defined

### JPA Entity Mapping ✅
- Entities correctly annotated with `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- Both column definitions and `@ManyToOne` relationships correctly mapped
- `insertable=false, updatable=false` on relationship columns prevents duplicate inserts

### Repository Layer ⚠️ Incomplete
- Basic Spring Data JPA repositories use method naming correctly
- **BUT**: Missing many queries needed for filtering/search (see section 3 above)
- No custom `@Query` annotations where needed for complex filters
- No Specifications for dynamic filtering

---

## 5. Testing Coverage 📊

### Test Files Present
- `TaskListControllerTest.java` ✅ — 230 lines, comprehensive
- `TaskListServiceTest.java` — Exists (not reviewed)
- `AuthServiceTest.java` — Exists
- `UserServiceTest.java` — Exists  
- `UserRepositoryTest.java` — Exists
- `AuthControllerTest.java` — Exists
- Test utilities: `TestDataBuilder.java`, `TestConfig.java`, `EnvLoaderExtension.java` ✅

### Coverage Assessment
- **TaskList operations**: Covered (GET, POST, PUT, DELETE with auth + error cases)
- **Authorization**: Good (tests verify 403 when user doesn't own resource)
- **Task operations**: **NOT COVERED** (no TaskController/Service to test)
- **WebSocket**: **NOT COVERED** (not implemented)
- **End-to-end auth flow**: Partially covered (OAuth2 flow tested in AuthServiceTest)

### Test Quality ✅
- Good use of MockMvc, Mockito, and AssertJ
- Proper test naming convention: `test{Method}_{Given}_{Should}` ✅
- Mocks are set up correctly
- Edge cases tested (404, 403, 409, 400)

**Recommendation**: Run `gradle test` to verify coverage before proceeding.

---

## 6. Code Quality 🔍

### Checkstyle Compliance ✅
- Project configured with Google Java Style (10.12.7) via `build.gradle`
- No commented code observed
- No TODO/FIXME comments found
- Proper formatting: camelCase variables, PascalCase classes

### Logging 📝
- Uses SLF4J correctly with `LOG` pattern
- GlobalExceptionHandler logs at appropriate levels (WARN for expected errors, ERROR for unexpected)
- AuthController missing one log statement (noted in Security section)

### Code Smells ⚠️
1. **Duplicate null check logic**: AuthController has same null check for authentication in getCurrentUser()
2. **String literals**: Status/priority stored as strings ("TO_DO", "MEDIUM") — consider enums
3. **Error message concatenation**: 
   ```java
   String message = "Provider " + oauthUserInfo.getProvider() + " already linked"; // ✅ acceptable
   ```

### Documentation 📚
- OpenAPI/Swagger annotations present on all endpoints ✅
- `@Operation`, `@Parameter`, `@ApiResponse` properly used
- CLAUDE.md provides comprehensive backend guidelines ✅

---

## 7. Configuration 🔧

### application.properties ✅ Well-Configured
- Database URL properly parameterized with environment variables
- MySQL connector configured (9.0.0)
- Flyway migrations enabled with sensible defaults
- OAuth2 providers (Google, GitHub) correctly configured with scopes
- Logging levels appropriate (DEBUG for taskmanager, INFO for root)
- WebSocket allowed origins configured
- Session security settings correct

### Issues Found
1. **Session cookie domain not applied** (discussed in Security section)
2. **spring.jpa.hibernate.ddl-auto=create** — For development only; should be `validate` in production

---

## 8. Dependencies 📦

### Current Stack (from build.gradle)
- Java 21 ✅
- Spring Boot 4.0.6 ✅ (latest as of Feb 2025)
- Spring Data JPA + Hibernate ✅
- Spring Security + OAuth2 client ✅
- MySQL Connector/J 9.0.0 ✅
- Flyway for migrations ✅
- Lombok for boilerplate reduction ✅
- SpringDoc OpenAPI 2.1.0 ✅ (Swagger integration)
- JUnit 5, Mockito, TestContainers ✅ (testing)

### No Security Issues Found
- All dependencies are production-ready and up-to-date
- No known CVEs in these versions (as of Feb 2025)

---

## 9. Error Handling ✅

### GlobalExceptionHandler Coverage
Properly handles:
- `ResourceNotFoundException` → 404
- `UnauthorizedException` → 403
- `ValidationException` → 400 (with field details)
- `ConflictException` → 409
- `MethodArgumentNotValidException` → 400 (Spring validation)
- Catch-all `Exception` → 500

### ErrorResponse DTO
Consistent structure:
```json
{
  "error": "message",
  "details": { "field": "validation rule" },
  "timestamp": "2026-05-18T...",
  "status": 400
}
```

---

## 10. Critical Action Items

| Priority | Item | File | Impact |
|----------|------|------|--------|
| 🔴 P0 | Implement Task Controller CRUD | `controller/TaskController.java` (missing) | Feature blocking |
| 🔴 P0 | Implement Task Service with auth checks | `service/TaskService.java` (missing) | Feature blocking |
| 🔴 P0 | Add Task query methods to repository | `repository/TaskRepository.java` | Feature blocking |
| 🔴 P0 | Implement WebSocket handlers & scheduler | `websocket/` (missing package) | Feature blocking |
| 🟡 P1 | Implement /api/seed endpoint | `controller/` | Testing/demo blocker |
| 🟡 P1 | Fix session cookie domain config | `config/SecurityConfig.java` | Production readiness |
| 🟡 P1 | Add logging to AuthController.getCurrentUser() | `controller/AuthController.java` line 52 | Debugging |
| 🟢 P2 | Consider Enum for status/priority | `entity/Task.java` | Code quality |
| 🟢 P2 | Add `@PreAuthorize` annotations | Services | Code clarity |

---

## 11. Strengths Summary ✅

1. **Well-architected**: Clean layer separation, proper use of Spring patterns
2. **Security-first**: OAuth2 correctly implemented, authorization checks in place, exception handling secure
3. **Professional testing**: Good test structure, mocking, assertion patterns
4. **Standards-compliant**: Follows naming conventions, checkstyle, OpenAPI docs
5. **Database design**: Proper schema with indexes, foreign keys, and constraints
6. **Configuration**: Environment-aware, flexible, and documented

---

## 12. Recommendations

### Immediate (Before Testing)
1. **Implement Task Controller** with all CRUD operations and authorization checks
2. **Implement Task Service** with proper user_id verification
3. **Expand TaskRepository** with query methods for filtering and search
4. **Add Session Cookie Config** to SecurityConfig for Docker deployment

### Short-term (Before Production)
1. Implement WebSocket layer for real-time notifications
2. Add /api/seed endpoint for sample data
3. Complete test coverage (Task operations, WebSocket)
4. Consider migrating status/priority to Enums

### Documentation
1. Update README with all missing features (Task CRUD endpoints, WebSocket message formats)
2. Document seed data structure
3. Clarify deletion policy for lists (cascade vs. block)

---

## 13. Testing the Changes

After implementing missing features, verify with:

```bash
# Build and run tests
gradle build

# Check code style
gradle checkstyleMain

# Run specific test
gradle test --tests com.taskmanager.controller.TaskControllerTest

# View coverage
open build/reports/jacoco/test/html/index.html
```

---

## Conclusion

The backend is **architecturally sound** but **feature-incomplete**. The missing Task layer and WebSocket implementation are **blocking** full functionality. Once those are implemented with proper authorization checks, the application will be ready for integration testing with the frontend.

**Overall Grade**: B+ (Architecture & Security: A, Completeness: D, Testing: B)

