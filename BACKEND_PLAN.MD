# Backend Implementation Plan for Personal To-Do Manager

## Context
This project requires building a full-stack Personal To-Do Manager with OAuth2 SSO (Google & GitHub), task management across multiple lists, and real-time WebSocket notifications. The backend needs to enforce single-user data isolation, provide REST APIs for CRUD operations, and push overdue/due-soon task notifications via WebSocket.

**Current State**: Spring Boot project initialized with base structure. Package: `com.taskmanager` (note: not `com.taskmanager`).
- ✅ `TaskManagerApplication.java` created
- ✅ `application.properties` created
- ⏳ Need to create all entities, services, controllers, configs, and migrations

---

## Phase 1: Project Structure & Configuration ✅ COMPLETE

- [x] Spring Boot project initialized with Gradle
- [x] `build.gradle` exists with Spring Boot 3.x dependencies
- [x] `TaskManagerApplication.java` created
- [x] `application.properties` created
- [x] Create `backend/src/main/resources/application-dev.properties` for local dev config

**Status**: 100% complete.

**Dev properties include**:
- MySQL 8 connection (localhost:3306, user: root, password: root)
- Hibernate validation mode, Flyway migrations enabled
- OAuth2 setup for Google & GitHub (env var defaults)
- WebSocket CORS for localhost:5173, localhost:3000
- Spring Security session config (non-secure for dev)

---

## Phase 2: Database Design & Migrations ✅ COMPLETE

**Prerequisite**: MySQL database and user created via `docker/init-db/` scripts.

- [x] Create `V001__initial_schema.sql` with users, lists, tasks tables
- [x] Add proper indexes and constraints
- [x] Create `V002__seed_data.sql` for sample data (optional)

**Files to Create**:
- [x] `backend/src/main/resources/db/migration/V001__initial_schema.sql`
- [x] `backend/src/main/resources/db/migration/V002__seed_data.sql`

**Status**: 100% complete. Schema includes users, user_auth_providers, task_lists, and tasks tables with appropriate indexes and foreign keys. Seed data available for development.

---

## Phase 3: JPA Entity Models ⏳ IN PROGRESS

- [ ] Create `User.java` entity (id: UUID, email, display_name, avatar_url, timestamps)
- [ ] Create `UserAuthProvider.java` entity (id: UUID, user_id, provider, provider_user_id)
- [ ] Create `TaskList.java` entity (id: UUID, user_id, title, timestamps)
- [ ] Create `Task.java` entity (id: UUID, list_id, user_id, title, description, status, priority, due_date, timestamps)
- [ ] Create `TaskStatus.java` enum (TO_DO, IN_PROGRESS, DONE)
- [ ] Create `TaskPriority.java` enum (LOW, MEDIUM, HIGH)
- [ ] Create `NotificationMessage.java` DTO for WebSocket messages

**Status**: Next phase to implement. Database schema exists; entities need to be created to map to schema.

**Files to Create**:
- [ ] `backend/src/main/java/com/taskmanager/entity/User.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/UserAuthProvider.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/TaskList.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/Task.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/TaskStatus.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/TaskPriority.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/NotificationMessage.java`

---

## Phase 4: Spring Data JPA Repositories ⏳ PENDING

- [ ] Create `UserRepository.java` with email lookup methods
- [ ] Create `UserAuthProviderRepository.java` with provider lookup methods
- [ ] Create `TaskListRepository.java` with user-filtered queries
- [ ] Create `TaskRepository.java` with search, filter, and notification queries

**Files to Create**:
- [ ] `backend/src/main/java/com/taskmanager/repository/UserRepository.java`
- [ ] `backend/src/main/java/com/taskmanager/repository/UserAuthProviderRepository.java`
- [ ] `backend/src/main/java/com/taskmanager/repository/TaskListRepository.java`
- [ ] `backend/src/main/java/com/taskmanager/repository/TaskRepository.java`

**Status**: Blocked by Phase 3 (entity creation).

---

## Phase 5: Service Layer with Business Logic ⏳ PENDING

- [ ] Create `UserService.java` for user profile management and multi-provider lookup
- [ ] Create `UserAuthProviderService.java` for linking/unlinking OAuth providers to users
- [ ] Create `ListService.java` with CRUD and authorization checks
- [ ] Create `TaskService.java` with CRUD, search, filters, and notification queries
- [ ] Create `AuthService.java` for OAuth2 user mapping: lookup by email, link provider, create session
- [ ] Create custom exceptions: `ResourceNotFoundException`, `UnauthorizedException`, `ValidationException`

**Status**: Blocked by Phase 4 (repository creation).

**Files to Create**:
- [ ] `backend/src/main/java/com/taskmanager/service/UserService.java`
- [ ] `backend/src/main/java/com/taskmanager/service/UserAuthProviderService.java`
- [ ] `backend/src/main/java/com/taskmanager/service/TaskListService.java`
- [ ] `backend/src/main/java/com/taskmanager/service/TaskService.java`
- [ ] `backend/src/main/java/com/taskmanager/service/AuthService.java`
- [ ] `backend/src/main/java/com/taskmanager/exception/ResourceNotFoundException.java`
- [ ] `backend/src/main/java/com/taskmanager/exception/UnauthorizedException.java`
- [ ] `backend/src/main/java/com/taskmanager/exception/ValidationException.java`

---

## Phase 6: REST Controllers & DTOs ⏳ PENDING

- [ ] Create all DTOs: UserDTO, ListDTO, TaskDTO, request/response objects
- [ ] Create `AuthController.java` with GET /api/auth/me
- [ ] Create `ListController.java` with CRUD endpoints
- [ ] Create `TaskController.java` with CRUD, search, filter, and status endpoints
- [ ] Create `SeedController.java` with POST /api/seed
- [ ] Create `GlobalExceptionHandler.java` for centralized error handling

**Status**: Blocked by Phase 5 (service layer creation).

**Files to Create**:
- [ ] `backend/src/main/java/com/taskmanager/dto/UserDTO.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/TaskListDTO.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/CreateTaskListRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/UpdateTaskListRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/TaskDTO.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/CreateTaskRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/UpdateTaskRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/UpdateTaskStatusRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/ErrorResponse.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/AuthController.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/TaskListController.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/TaskController.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/SeedController.java`
- [ ] `backend/src/main/java/com/taskmanager/exception/GlobalExceptionHandler.java`

---

## Phase 7: OAuth2 Security Configuration ⏳ PENDING

- [ ] Create `SecurityConfig.java` with OAuth2 login and CSRF protection
- [ ] Create `OAuth2UserService.java` for OAuth2 user mapping
- [ ] Create `SecurityUser.java` custom UserDetails implementation
- [ ] Update `application.properties` with OAuth2 client configuration

**Files to Create**:
- [ ] `backend/src/main/java/com/taskmanager/config/SecurityConfig.java`
- [ ] `backend/src/main/java/com/taskmanager/security/OAuth2UserService.java`
- [ ] `backend/src/main/java/com/taskmanager/security/SecurityUser.java`

**Status**: Can proceed in parallel with Phase 5/6.

---

## Phase 8: WebSocket Configuration & Notifications ⏳ PENDING

- [ ] Create `WebSocketConfig.java` to enable and configure WebSocket
- [ ] Create `TaskNotificationHandler.java` to handle WebSocket messages
- [ ] Create `NotificationScheduler.java` for periodic notification checks
- [ ] Implement message types: notification, subscribe, ack

**Files to Create**:
- [ ] `backend/src/main/java/com/taskmanager/config/WebSocketConfig.java`
- [ ] `backend/src/main/java/com/taskmanager/websocket/TaskNotificationHandler.java`
- [ ] `backend/src/main/java/com/taskmanager/websocket/NotificationScheduler.java`

**Status**: Blocked by Phase 5 (TaskService dependency).

---

## Phase 9: Comprehensive Test Suite ⏳ PENDING

- [ ] Create `UserServiceTest.java`
- [ ] Create `ListServiceTest.java`
- [ ] Create `TaskServiceTest.java`
- [ ] Create `AuthControllerTest.java`
- [ ] Create `ListControllerTest.java`
- [ ] Create `TaskControllerTest.java`
- [ ] Create `TaskNotificationHandlerTest.java`
- [ ] Create `TestDataBuilder.java` utility for tests
- [ ] Run all tests: `gradle test`
- [ ] Verify 70%+ code coverage

**Status**: Created incrementally alongside service/controller development.

**Files to Create**:
- [ ] `backend/src/test/java/com/taskmanager/service/UserServiceTest.java`
- [ ] `backend/src/test/java/com/taskmanager/service/TaskListServiceTest.java`
- [ ] `backend/src/test/java/com/taskmanager/service/TaskServiceTest.java`
- [ ] `backend/src/test/java/com/taskmanager/controller/AuthControllerTest.java`
- [ ] `backend/src/test/java/com/taskmanager/controller/TaskListControllerTest.java`
- [ ] `backend/src/test/java/com/taskmanager/controller/TaskControllerTest.java`
- [ ] `backend/src/test/java/com/taskmanager/websocket/TaskNotificationHandlerTest.java`
- [ ] `backend/src/test/java/com/taskmanager/util/TestDataBuilder.java`

---

## Phase 10: Documentation & Setup ⏳ PENDING

- [ ] Update `README.md` with local setup, OAuth setup, API docs
- [ ] Create `.env.example` with required environment variables
- [ ] Document WebSocket message format
- [ ] Document notification rules
- [ ] Document deletion behavior choice
- [ ] Add seed data instructions

**Files to Create/Update**:
- [ ] Update `README.md`
- [ ] Create `.env.example`

**Status**: Final phase; completed after Phases 6-8 complete.

---

## Critical Implementation Details

### Authorization Strategy
- Every endpoint calls `getCurrentUser()` from SecurityContext
- Verify resource belongs to user: throw `UnauthorizedException` if mismatch
- Soft deletes: queries filter `deleted_at IS NULL`

### Deletion Behavior
**Block pattern** (document in README):
- Deleting a list returns 409 Conflict if it has tasks
- User must delete all tasks first, then delete the list
- Deletion logic handled in service layer (no database cascade)
- Prevents accidental data loss

### WebSocket Notification Rules
- **Overdue**: `status != DONE AND due_date < TODAY`
- **Due Soon**: `status != DONE AND due_date >= TODAY AND due_date <= TODAY + 3 days`
- Send on connect + periodic (60s default, configurable)

---

## Local Setup Verification

**Prerequisites**:
- [ ] MySQL database created (via `docker/init-db/` scripts or docker-compose)

**Steps**:
- [ ] Build backend: `gradle build`
- [ ] Run tests: `gradle test`
- [ ] Set Env variables for `/backend/.env`
- [ ] Start backend: `gradle bootRun`
- [ ] Start frontend: `npm run dev`

### Manual Testing Checklist
- [ ] Login with Google OAuth
- [ ] Login with GitHub OAuth
- [ ] Create a list
- [ ] Create a task in the list
- [ ] Search tasks by title/description
- [ ] Filter by status, priority, due date
- [ ] Update task status via PATCH
- [ ] Delete task
- [ ] WebSocket connect → receive notifications
- [ ] Cross-user isolation: verify User A cannot see User B's data

---

## Summary

**Total files to create**: ~45 files

**Key directories** (using `com.taskmanager` package):
```
backend/
├── build.gradle ✓
├── src/main/java/com/taskmanager/
│   ├── TaskManagerApplication.java ✓
│   ├── config/ (SecurityConfig, WebSocketConfig)
│   ├── controller/ (Auth, TaskList, Task, Seed, GlobalExceptionHandler)
│   ├── service/ (User, TaskList, Task, Auth)
│   ├── entity/ (User, TaskList, Task, enums)
│   ├── repository/ (UserRepository, TaskListRepository, TaskRepository)
│   ├── dto/ (all request/response DTOs)
│   ├── websocket/ (TaskNotificationHandler, NotificationScheduler)
│   ├── security/ (OAuth2UserService, SecurityUser)
│   └── exception/ (custom exceptions)
├── src/main/resources/
│   ├── application.properties ✓
│   ├── application-dev.properties
│   └── db/migration/ (V001, V002)
├── src/test/java/com/taskmanager/
│   ├── TaskManagerApplicationTests.java ✓
│   ├── service/ (service tests)
│   ├── controller/ (controller tests)
│   ├── websocket/ (WebSocket tests)
│   └── util/ (TestDataBuilder)
```
