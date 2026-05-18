# Backend Implementation Plan for Personal To-Do Manager

## Context
This project requires building a full-stack Personal To-Do Manager with OAuth2 SSO (Google & GitHub), task management across multiple lists, and real-time WebSocket notifications. The backend needs to enforce single-user data isolation, provide REST APIs for CRUD operations, and push overdue/due-soon task notifications via WebSocket.

**Current State**: Spring Boot project initialized with Phase 1-2 complete. Package: `com.taskmanager`.
- ✅ `TaskManagerApplication.java` created
- ✅ `application.properties` created
- ✅ Database migrations (V001, V002) created
- ✅ Exception handling framework (Phase 1.3) complete
- ✅ Entity domain model defined (Phase 1.3)
- ✅ Test utilities ready (Phase 1.3)
- ✅ OpenAPI/Swagger setup (Phase 1.4) complete
- ✅ Authentication feature (Phase 2) complete - OAuth2 login, user profile, session persistence
- ⏳ Ready to start Phase 3: Lists Feature

---

## Why Feature-Based Structure?

**Previous approach (layer-based)**: Build all entities → all repositories → all services → all controllers → all tests
- ❌ Integration risk deferred: nothing works until Phase 6+
- ❌ Testing at the end: can't validate during development
- ❌ Parallelization blocked: each layer waits for previous layer
- ❌ No runnable app until late stages

**New approach (feature-based)**: Build Auth end-to-end → Lists end-to-end → Tasks end-to-end → WebSocket end-to-end
- ✅ Early integration testing: catch bugs after each feature
- ✅ Concurrent testing: each phase includes tests
- ✅ Runnable app after Phase 2: can demo auth before tasks
- ✅ Parallel work: UI team can start after Phase 2
- ✅ Clear dependencies: Auth → Lists → Tasks → WebSocket

---

## Tier Dependency Graph

```
Phase 1: Shared Infrastructure (Project, DB, Config, Exception Handling)
    ↓
Phase 2: Authentication Feature ← All other features depend on this
    ├─→ Phase 3: Lists Feature
    │     ├─→ Phase 4: Tasks Feature
    │           ├─→ Phase 5: WebSocket Feature
    └─→ Can build Lists/Tasks/WebSocket in parallel after Auth
    
Phase 6: Polish & Final Integration
```

---

## Phase 1: Shared Infrastructure ✅ COMPLETE

**Prerequisite**: MySQL database and user created via `docker/init-db/` scripts.

### 1.1 Project Setup & Configuration ✅
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

### 1.2 Database Design & Migrations ✅
- [x] Create `V001__initial_schema.sql` with users, lists, tasks tables
- [x] Add proper indexes and constraints
- [x] Create `V002__seed_data.sql` for sample data

**Files**: 
- [x] `backend/src/main/resources/db/migration/V001__initial_schema.sql`
- [x] `backend/src/main/resources/db/migration/V002__seed_data.sql`

**Status**: 100% complete. Schema includes users, user_auth_providers, task_lists, and tasks tables with appropriate indexes and foreign keys.

### 1.3 Shared Infrastructure Components ✅ COMPLETE

- [x] Create `GlobalExceptionHandler.java` for centralized error handling
- [x] Create custom exception classes:
  - [x] `ResourceNotFoundException.java`
  - [x] `UnauthorizedException.java`
  - [x] `ValidationException.java`
  - [x] `ConflictException.java` (added)
- [x] Create `TestDataBuilder.java` utility in test folder for test data generation
- [x] Create Entity classes (User, UserAuthProvider, TaskList, Task) for domain model
- [x] Verify Spring Security and OAuth2 dependencies are in build.gradle

**Files Created**:
- [x] `backend/src/main/java/com/taskmanager/exception/ResourceNotFoundException.java`
- [x] `backend/src/main/java/com/taskmanager/exception/UnauthorizedException.java`
- [x] `backend/src/main/java/com/taskmanager/exception/ValidationException.java`
- [x] `backend/src/main/java/com/taskmanager/exception/ConflictException.java`
- [x] `backend/src/main/java/com/taskmanager/config/GlobalExceptionHandler.java`
- [x] `backend/src/main/java/com/taskmanager/dto/ErrorResponse.java`
- [x] `backend/src/main/java/com/taskmanager/entity/User.java`
- [x] `backend/src/main/java/com/taskmanager/entity/UserAuthProvider.java`
- [x] `backend/src/main/java/com/taskmanager/entity/TaskList.java`
- [x] `backend/src/main/java/com/taskmanager/entity/Task.java`
- [x] `backend/src/test/java/com/taskmanager/util/TestDataBuilder.java`

**Status**: Shared foundation for all other phases. Complete. Ready for Phase 2.

**Deliverable**: 
- ✅ Exception handling framework in place
- ✅ Global error response format standardized
- ✅ Entity model defined
- ✅ Test utilities ready
- ✅ No manual testing needed for Phase 1.3 (infrastructure layer)

### 1.4 OpenAPI / Swagger Documentation ⏳ PENDING

- [ ] Add SpringDoc OpenAPI dependency to `build.gradle`
  - `implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'`
- [ ] Configure OpenAPI in `application.properties`:
  ```properties
  springdoc.api-docs.path=/api-docs
  springdoc.swagger-ui.path=/swagger-ui.html
  springdoc.swagger-ui.operationsSorter=method
  ```
- [ ] Create OpenAPI info bean in `SecurityConfig.java`:
  - Add `@Bean` method returning `OpenAPI` with title, version, description, contact
  - Example: Title="Personal To-Do Manager API", Version="1.0.0"

**Files**:
- [ ] Update `build.gradle` with springdoc dependency
- [ ] Update `application.properties` with OpenAPI config
- [ ] Add OpenAPI bean to `backend/src/main/java/com/taskmanager/config/OpenApiConfig.java` (new file)

**Status**: OpenAPI foundation configured. Ready for Phase 2+ controllers to add `@Operation`, `@Parameter` annotations.

**Deliverable**: 
- ✅ SpringDoc OpenAPI UI accessible at `http://localhost:8080/swagger-ui.html`
- ✅ `/api-docs` endpoint serving OpenAPI 3.0 spec (JSON)
- ✅ Configuration ready for controller annotations in Phase 2+

---

## Phase 2: Authentication Feature ✅ COMPLETE

**Prerequisite**: Phase 1 complete (including 1.4 OpenAPI setup).

**Goal**: User can OAuth login (Google/GitHub), see profile, and stay logged in across page refresh. Foundation for all other features.

### 2.1 User & Auth Provider Entities ✅
- [x] Create `User.java` entity
  - Fields: id (UUID), email (UNIQUE), display_name, avatar_url, created_at, updated_at
  - Relationships: One-to-many with UserAuthProvider
- [x] Create `UserAuthProvider.java` entity
  - Fields: id (UUID), user_id (FK), provider (enum), provider_user_id, created_at
  - Constraints: UNIQUE (provider, provider_user_id), UNIQUE (user_id, provider)
  - Relationships: Many-to-one with User

**Files**:
- [x] `backend/src/main/java/com/taskmanager/entity/User.java`
- [x] `backend/src/main/java/com/taskmanager/entity/UserAuthProvider.java`

### 2.2 Repositories ✅
- [x] Create `UserRepository.java`
  - `findByEmail(String email)`: for OAuth user lookup
  - `findById(UUID id)`: retrieve user
- [x] Create `UserAuthProviderRepository.java`
  - `findByProviderAndProviderUserId(String provider, String providerUserId)`: OAuth provider lookup
  - `findByUserIdAndProvider(UUID userId, String provider)`: multi-provider per user

**Files**:
- [x] `backend/src/main/java/com/taskmanager/repository/UserRepository.java`
- [x] `backend/src/main/java/com/taskmanager/repository/UserAuthProviderRepository.java`

### 2.3 Service Layer ✅
- [x] Create `UserService.java`
  - `getUserById(UUID id)`: retrieve user, verify not deleted
  - `getUserByEmail(String email)`: find user by email
  - `updateUser(UUID id, UserDTO dto)`: update profile (name, avatar)
  - Authorization: verify current user owns resource
- [x] Create `AuthService.java`
  - `findOrCreateUserFromOAuth(OAuthUserInfo)`: core OAuth flow
  - `linkOAuthProvider(User, OAuthUserInfo)`: link multiple providers to one email
  - `validateAuthProvider(UUID userId, String provider)`: security check

**Files**:
- [x] `backend/src/main/java/com/taskmanager/service/UserService.java`
- [x] `backend/src/main/java/com/taskmanager/service/AuthService.java`

### 2.4 Security & OAuth2 Configuration ✅
- [x] Create `SecurityConfig.java`
  - OAuth2 login configuration (Google + GitHub)
  - CSRF protection + session config
  - Redirect on success/failure
  - Logout endpoint
- [x] Create `CustomOAuth2UserService.java` (renamed from OAuth2UserService)
  - Implements `DefaultOAuth2UserService`
  - Maps OAuth provider attributes → User + UserAuthProvider entities
  - Calls `AuthService.findOrCreateUserFromOAuth()`
- [x] Create `SecurityUser.java`
  - Implements `UserDetails`
  - Wraps User entity for Spring Security
  - getCurrentUser() helper

**Files**:
- [x] `backend/src/main/java/com/taskmanager/config/SecurityConfig.java`
- [x] `backend/src/main/java/com/taskmanager/security/CustomOAuth2UserService.java`
- [x] `backend/src/main/java/com/taskmanager/security/SecurityUser.java`

### 2.5 DTOs & Controller ✅
- [x] Create DTOs:
  - [x] `UserDTO.java` (id, email, displayName, avatarUrl)
  - [x] `UserProfileResponse.java` (response DTO for GET /api/auth/me)
  - [x] `OAuthUserInfo.java` (OAuth provider mapping)
- [x] Create `AuthController.java`
  - `GET /api/auth/me`: return current user profile
  - `POST /api/auth/logout`: logout user
  - Uses `SecurityContextHolder` from Spring Security
  - Returns 401 if not authenticated

**Files**:
- [x] `backend/src/main/java/com/taskmanager/dto/UserDTO.java`
- [x] `backend/src/main/java/com/taskmanager/dto/UserProfileResponse.java`
- [x] `backend/src/main/java/com/taskmanager/dto/OAuthUserInfo.java`
- [x] `backend/src/main/java/com/taskmanager/controller/AuthController.java`

### 2.6 Tests ✅
- [x] Create `UserServiceTest.java`
  - Test user creation, retrieval, updates
  - Test authorization (can't access other user's data)
- [x] Create `AuthServiceTest.java`
  - Test OAuth provider mapping
  - Test multi-provider linking
  - Test findOrCreate with new vs existing users
- [x] Create `AuthControllerTest.java`
  - Mock OAuth authentication
  - Test GET /api/auth/me with authenticated user
  - Test 401 without authentication

**Files**:
- [x] `backend/src/test/java/com/taskmanager/service/UserServiceTest.java`
- [x] `backend/src/test/java/com/taskmanager/service/AuthServiceTest.java`
- [x] `backend/src/test/java/com/taskmanager/controller/AuthControllerTest.java`

### 2.7 Documentation ⏳ PENDING
- [ ] Update README.md with:
  - OAuth2 setup for Google (credentials, redirect URI)
  - OAuth2 setup for GitHub (credentials, redirect URI)
  - Required environment variables
  - How session persistence works (see Session-Based Authentication section below)
  - Manual testing: "Login with Google/GitHub" flow
- [ ] Verify OpenAPI annotations in `AuthController.java`:
  - All endpoints documented with `@Operation` and `@Parameter` ✅ Already done
  - OpenAPI spec accessible at `GET /api-docs` ✅ Already accessible
  - Swagger UI displays auth endpoints at `/swagger-ui.html` ✅ Already accessible

**Status**: Foundation feature complete. All other features depend on this. App is runnable with OAuth login.

**Deliverable**: ✅ User can OAuth login (Google/GitHub), see profile via `GET /api/auth/me`, stay logged in across refresh. ✅ Tests passing. ✅ OpenAPI annotations in place, Swagger UI shows auth endpoints. ⏳ README OAuth2 documentation pending.

---

## Session-Based Authentication Architecture

### Overview
This app uses **Spring Session** with HttpOnly cookies for authentication, not JWT tokens.

**Flow**:
1. User clicks "Login with Google/GitHub"
2. Backend exchanges OAuth code for user info
3. Backend creates session (stored server-side)
4. Browser receives HttpOnly, Secure session cookie
5. Browser automatically sends cookie with every request (same-origin)
6. Spring SecurityContext restores authenticated user from session
7. Page refresh: Session cookie sent automatically → authenticated ✅

**Security Benefits**:
- ✅ Token never exposed to JavaScript (XSS protection via HttpOnly)
- ✅ CSRF protection built-in (Spring Security)
- ✅ Works across tabs automatically
- ✅ Logout just invalidates server session

### Local Development Setup (Hosts File + Vite Proxy)

**Problem**: Frontend (localhost:5173) and backend (localhost:8080) are different origins.

**Solution**: Use hosts file to make both same domain, Vite proxy to rewrite cookies.

1. Edit `/etc/hosts`:
   ```
   127.0.0.1 app.localhost
   127.0.0.1 api.localhost
   ```

2. Start backend on `http://api.localhost:8080`:
   ```bash
   cd backend && ./gradlew bootRun
   ```

3. Start frontend on `http://app.localhost:5173` (Vite proxy handles routing):
   ```bash
   cd ui && npm run dev
   # Vite dev server proxies /api to http://api.localhost:8080
   ```

4. Configure backend cookie domain in `SecurityConfig.java`:
   ```java
   http.sessionManagement(session -> session
     .sessionCookieConfig(config -> {
       config.setDomain(".localhost");  // Covers both app.localhost & api.localhost
       config.setPath("/");
       config.setHttpOnly(true);
       config.setSecure(false);  // Allow HTTP in dev
     })
   );
   ```

5. Vite config (UI_PLAN.MD handles this) already has:
   - Proxy `/api` to `http://api.localhost:8080`
   - Cookie domain rewrite to `.localhost`

**Result**: Session cookies work across page refresh ✅

### Docker-Compose Architecture (Production)

**Reverse Proxy Pattern** (Nginx routes both):
```
User Browser → http://localhost (single origin)
    ↓
Nginx (Port 80)
    ├── GET / → Vue SPA static files
    ├── POST /api/* → Spring backend :8080
    └── GET /api/ws → WebSocket :8080

Result: Single origin, cookies work ✅
```

**Backend Configuration for Docker**:
```java
// In SecurityConfig.java
http.sessionManagement(session -> session
  .sessionCookieConfig(config -> {
    config.setDomain("localhost");  // or production domain
    config.setPath("/");
    config.setHttpOnly(true);
    config.setSecure(true);  // HTTPS in production
    config.setSameSite("Lax");
  })
);
```

**Implementation Plan** (Phase 6: Polish):
- [ ] Create `docker/nginx/default.conf` routing `/` to frontend, `/api` and `/api/ws` to backend
- [ ] Update `docker-compose.yml` to add Nginx service (see `.claude/rules/reverse-proxy-setup.md`)
- [ ] Document reverse proxy setup in README
- [ ] Test session persistence: login → refresh page → still authenticated

### OAuth Redirect URI Configuration

**Local Development**:
- OAuth provider redirect URI: `http://app.localhost:5173/oauth/callback`
- Backend initiates OAuth login: `http://api.localhost:8080/oauth2/authorization/google`

**Docker (Production)**:
- OAuth provider redirect URI: `http://localhost/oauth/callback`
- Backend initiates OAuth login: `http://localhost/oauth2/authorization/google`

### Testing Session Persistence

**Local Dev Test**:
1. Open `http://app.localhost:5173`
2. Click "Login with Google/GitHub"
3. Complete OAuth flow
4. Verify `GET /api/auth/me` returns user profile
5. Refresh page (Ctrl+R)
6. User still authenticated, profile still visible ✅
7. Check DevTools → Application → Cookies → session cookie present

---

## Phase 3: Lists (Projects) Feature ⏳ IN PROGRESS

**Prerequisite**: Phase 2 complete (Auth required) - ✅ Complete!

**Goal**: User can CRUD their task lists/projects. Authorization enforced.

### 3.1 TaskList Entity
- [ ] Create `TaskList.java` entity
  - Fields: id (UUID), user_id (FK → User), title, created_at, updated_at
  - Relationships: Many-to-one with User, One-to-many with Task

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/entity/TaskList.java`

### 3.2 Repository
- [ ] Create `TaskListRepository.java`
  - `findByUserId(UUID userId)`: get all user's lists
  - `findByIdAndUserId(UUID id, UUID userId)`: verify ownership
  - Custom query for paginated retrieval

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/repository/TaskListRepository.java`

### 3.3 Service Layer
- [ ] Create `TaskListService.java`
  - `createList(UUID userId, String title)`: create with ownership
  - `getListsByUser(UUID userId)`: retrieve all user's lists
  - `getListById(UUID id, UUID userId)`: verify ownership, throw UnauthorizedException
  - `updateList(UUID id, UUID userId, String newTitle)`: verify ownership
  - `deleteList(UUID id, UUID userId)`: return 409 if list has tasks, prevent cascade
  - All methods verify getCurrentUser() == userId

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/service/TaskListService.java`

### 3.4 DTOs & Controller
- [ ] Create DTOs:
  - [ ] `TaskListDTO.java` (id, title, createdAt, updatedAt)
  - [ ] `CreateTaskListRequest.java` (title)
  - [ ] `UpdateTaskListRequest.java` (title)
- [ ] Create `TaskListController.java`
  - `GET /api/lists`: get all user's lists
  - `POST /api/lists`: create new list
  - `PUT /api/lists/{id}`: update list
  - `DELETE /api/lists/{id}`: delete list (409 if not empty)

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/dto/TaskListDTO.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/CreateTaskListRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/UpdateTaskListRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/TaskListController.java`

### 3.5 Tests
- [ ] Create `TaskListServiceTest.java`
  - Test CRUD operations
  - Test authorization (can't access other user's lists)
  - Test deletion blocking when tasks exist
- [ ] Create `TaskListControllerTest.java`
  - Test all endpoints
  - Test 403 when accessing other user's list
  - Test 409 on delete with tasks

**Files**:
- [ ] `backend/src/test/java/com/taskmanager/service/TaskListServiceTest.java`
- [ ] `backend/src/test/java/com/taskmanager/controller/TaskListControllerTest.java`

### 3.6 Documentation
- [ ] Update README.md with:
  - List API endpoints (GET, POST, PUT, DELETE)
  - Response examples
  - Deletion behavior explanation (block-if-not-empty, no cascade)
  - Error codes (409 Conflict)

**Status**: Lists feature complete and tested.

**Deliverable**: User can create/read/update/delete lists. Authorization verified. Deletion behavior enforced. Tests passing. List API docs complete.

---

## Phase 4: Tasks Feature ⏳ PENDING

**Prerequisite**: Phase 2 (Auth) and Phase 3 (Lists) complete.

**Goal**: Full task management with CRUD, search, filters, status updates, and seed data.

### 4.1 Task Entities & Enums
- [ ] Create `TaskStatus.java` enum (TO_DO, IN_PROGRESS, DONE)
- [ ] Create `TaskPriority.java` enum (LOW, MEDIUM, HIGH)
- [ ] Create `Task.java` entity
  - Fields: id (UUID), list_id (FK), user_id (FK), title, description, status, priority, due_date (nullable), created_at, updated_at
  - Relationships: Many-to-one with TaskList, Many-to-one with User

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/entity/TaskStatus.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/TaskPriority.java`
- [ ] `backend/src/main/java/com/taskmanager/entity/Task.java`

### 4.2 Repository with Queries
- [ ] Create `TaskRepository.java`
  - `findByListId(UUID listId)`: get all tasks in list
  - `findByIdAndUserId(UUID id, UUID userId)`: verify ownership
  - `findByUserIdAndStatus(UUID userId, TaskStatus status)`: filter by status
  - `findByUserIdAndPriority(UUID userId, TaskPriority priority)`: filter by priority
  - `findOverdueTasks(UUID userId)`: due_date < TODAY AND status != DONE
  - `findDueSoonTasks(UUID userId)`: TODAY <= due_date <= TODAY + 3 days AND status != DONE
  - `searchByTitleOrDescription(UUID userId, String query)`: case-insensitive substring search

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/repository/TaskRepository.java`

### 4.3 Service Layer
- [ ] Create `TaskService.java`
  - `createTask(UUID userId, UUID listId, CreateTaskRequest)`: create with ownership
  - `getTasksByList(UUID userId, UUID listId)`: retrieve all tasks in list
  - `getTaskById(UUID id, UUID userId)`: verify ownership, throw if not found
  - `updateTask(UUID id, UUID userId, UpdateTaskRequest)`: verify ownership
  - `updateTaskStatus(UUID id, UUID userId, TaskStatus newStatus)`: quick status change
  - `deleteTask(UUID id, UUID userId)`: soft delete
  - `searchTasks(UUID userId, String query)`: search title/description
  - `filterTasks(UUID userId, TaskStatus status, TaskPriority priority, LocalDate dueDate)`: multi-filter
  - `getOverdueNotifications(UUID userId)`: for WebSocket
  - `getDueSoonNotifications(UUID userId)`: for WebSocket
  - All methods verify getCurrentUser() == userId

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/service/TaskService.java`

### 4.4 DTOs & Controller
- [ ] Create DTOs:
  - [ ] `TaskDTO.java` (id, listId, title, description, status, priority, dueDate, createdAt)
  - [ ] `CreateTaskRequest.java` (listId, title, description, status, priority, dueDate)
  - [ ] `UpdateTaskRequest.java` (title, description, priority, dueDate)
  - [ ] `UpdateTaskStatusRequest.java` (status)
- [ ] Create `TaskController.java`
  - `GET /api/tasks?list_id={id}`: get tasks in list
  - `GET /api/tasks/search?q={query}`: search tasks
  - `GET /api/tasks?status={status}&priority={priority}&due_date={filter}`: filter tasks
  - `POST /api/tasks`: create task
  - `PUT /api/tasks/{id}`: update task
  - `PATCH /api/tasks/{id}/status`: quick status change
  - `DELETE /api/tasks/{id}`: delete task
- [ ] Create `SeedController.java`
  - `POST /api/seed`: populate database with sample data for current user
  - Creates sample lists and tasks for testing

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/dto/TaskDTO.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/CreateTaskRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/UpdateTaskRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/UpdateTaskStatusRequest.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/TaskController.java`
- [ ] `backend/src/main/java/com/taskmanager/controller/SeedController.java`

### 4.5 Tests
- [ ] Create `TaskServiceTest.java`
  - Test CRUD operations
  - Test authorization (can't access other user's tasks)
  - Test search functionality
  - Test filters (status, priority, due date)
  - Test overdue/due-soon queries
- [ ] Create `TaskControllerTest.java`
  - Test all endpoints
  - Test 403 when accessing other user's tasks
  - Test seed endpoint creates data

**Files**:
- [ ] `backend/src/test/java/com/taskmanager/service/TaskServiceTest.java`
- [ ] `backend/src/test/java/com/taskmanager/controller/TaskControllerTest.java`

### 4.6 Documentation
- [ ] Update README.md with:
  - Task API endpoints (GET, POST, PUT, PATCH, DELETE)
  - Search endpoint documentation
  - Filter examples (status, priority, due date: overdue/today/next_7_days/all)
  - Response examples
  - Seed endpoint usage

**Status**: Core feature complete. MVP now fully functional.

**Deliverable**: User can create/read/update/delete/search/filter tasks. Status updates working. Seed data available. Tests passing. Task API docs complete.

---

## Phase 5: WebSocket Notifications ⏳ PENDING

**Prerequisite**: Phase 2 (Auth) and Phase 4 (Tasks) complete.

**Goal**: Real-time push notifications for overdue and due-soon tasks. Two-way WebSocket communication.

### 5.1 WebSocket Configuration
- [ ] Create `WebSocketConfig.java`
  - Enable WebSocket with `@EnableWebSocketMessageBroker`
  - Configure STOMP endpoint: `/api/ws`
  - Configure message broker (simple, in-memory for MVP)
  - CORS setup for frontend (localhost:5173, localhost:3000)

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/config/WebSocketConfig.java`

### 5.2 WebSocket Handler & Messages
- [ ] Create `TaskNotificationHandler.java`
  - Implement `WebSocketHandler` or STOMP controller
  - `afterConnectionEstablished()`: send initial batch of overdue/due-soon tasks on connect
  - Handle client messages: "subscribe" (custom interval), "ack" (mark notification read)
  - Send notifications: JSON with type, id, taskId, title, reason, due_date
- [ ] Create `NotificationMessage.java` DTO
  - Fields: type (notification), id (UUID), taskId (UUID), title, reason (overdue|due-soon), dueDate

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/websocket/TaskNotificationHandler.java`
- [ ] `backend/src/main/java/com/taskmanager/dto/NotificationMessage.java`

### 5.3 Notification Scheduler
- [ ] Create `NotificationScheduler.java`
  - `@Scheduled(fixedRate = 60000)` periodic check (60s default, configurable)
  - For each connected user: query overdue + due-soon tasks
  - Send via WebSocket to connected clients
  - Track sent notifications to avoid duplicates

**Files**:
- [ ] `backend/src/main/java/com/taskmanager/websocket/NotificationScheduler.java`

### 5.4 Tests
- [ ] Create `TaskNotificationHandlerTest.java`
  - Test WebSocket connection establishes
  - Test initial notification batch sent on connect
  - Test periodic notifications sent
  - Test client subscribe message changes interval
  - Test client ack message acknowledged
  - Test authorization: only current user receives their notifications

**Files**:
- [ ] `backend/src/test/java/com/taskmanager/websocket/TaskNotificationHandlerTest.java`

### 5.5 Documentation
- [ ] Update README.md with:
  - WebSocket endpoint: `ws://server/api/ws`
  - Server → Client message format (notification)
  - Client → Server message formats (subscribe, ack)
  - Notification rules (overdue, due-soon)
  - Example subscription flow

**Status**: Real-time feature complete.

**Deliverable**: WebSocket notifications working. Overdue/due-soon tasks pushed to client. Two-way communication established. Tests passing. WebSocket docs complete.

---

## Phase 6: Polish & Final Integration ⏳ PENDING

**Prerequisite**: Phases 1-5 complete.

**Goal**: Production-ready backend with complete documentation and verified test coverage.

### 6.1 Environment Configuration
- [ ] Create `.env.example` with all required variables:
  ```
  # OAuth
  GOOGLE_CLIENT_ID=your_google_client_id
  GOOGLE_CLIENT_SECRET=your_google_client_secret
  GITHUB_CLIENT_ID=your_github_client_id
  GITHUB_CLIENT_SECRET=your_github_client_secret
  
  # Database
  DB_HOST=localhost
  DB_PORT=3306
  DB_NAME=task_manager
  DB_USER=root
  DB_PASSWORD=root
  
  # App
  SERVER_PORT=8080
  SESSION_TIMEOUT=1800
  ```

**Files**:
- [ ] `.env.example`

### 6.2 README Final Pass
- [ ] Verify all sections complete and accurate:
  - Local setup (Java 21, MySQL, Gradle)
  - OAuth setup (Google + GitHub)
  - Environment variables
  - List API documentation
  - Task API documentation (including search/filters)
  - WebSocket message formats
  - Notification rules
  - Deletion behavior choice
  - Seed endpoint usage
  - Test commands (`gradle test`)
  - Build commands (`gradle build`)
  - **OpenAPI/Swagger**: How to access Swagger UI at `/swagger-ui.html` and `/api-docs`
- [ ] Add summary of all features

**Files**:
- [ ] Update README.md

### 6.3 Integration Testing
- [ ] Verify end-to-end flows:
  - OAuth login (Google + GitHub)
  - Create list → create task → update status → delete task → delete list
  - Search and filter tasks
  - WebSocket connect → receive initial notifications → subscribe → ack
  - Cross-user isolation: User A cannot see User B's data
- [ ] Manual testing checklist provided in README

### 6.4 Code Quality & Coverage
- [ ] Run `gradle build` locally
- [ ] Run `gradle test` and verify all tests pass
- [ ] Check test coverage ≥70%
- [ ] Code review for:
  - Consistent error handling
  - Authorization checks on all endpoints
  - Input validation
  - SQL injection prevention (using JPA)
  - XSS prevention (server-side validation)

### 6.5 Final Verification
- [ ] `gradle bootRun` starts successfully
- [ ] MySQL migrations run without errors
- [ ] OAuth login works end-to-end
- [ ] API endpoints respond correctly
- [ ] WebSocket connects and sends notifications
- [ ] No console errors or warnings

**Status**: Backend complete and production-ready.

**Deliverable**: All features working. Tests passing. Documentation complete. Deployable backend.

---

## Critical Implementation Details

### Authorization Strategy
- Every endpoint calls `getCurrentUser()` from SecurityContext
- Verify resource belongs to user: throw `UnauthorizedException` if mismatch
- Hard deletes only: immediately remove from database (no soft delete tracking)
- Example: `TaskService.getTaskById()` checks `task.user_id == getCurrentUser().id`

### Deletion Behavior (Block Pattern, Hard Delete)
- Deleting a list returns 409 Conflict if it has tasks
- User must delete all tasks first, then delete the list
- Deletion logic handled in service layer (no database CASCADE, immediate hard delete)
- Prevents accidental data loss
- Documented in README

### WebSocket Notification Rules
- **Overdue**: `status != DONE AND due_date < TODAY`
- **Due Soon**: `status != DONE AND TODAY <= due_date <= TODAY + 3 days`
- Send on connect (initial batch) + periodic checks (60s default, configurable)
- Notifications are one-way Server → Client for UI toast display
- Client can subscribe (custom interval) and ack (mark read)

### Testing Strategy
- **Unit Tests**: Service layer with mocked repositories
- **Integration Tests**: Controller layer with `@WebMvcTest` + mocked services
- **Authorization Tests**: Verify cross-user isolation
- **Data Tests**: `TestDataBuilder` for consistent test data
- **Coverage Goal**: ≥70% code coverage

---

## Integration Checkpoints

After each phase completes, verify:

**After Phase 1**:
- `gradle build` succeeds
- MySQL migrations run
- No missing dependencies
- OpenAPI UI accessible at `http://localhost:8080/swagger-ui.html`
- `/api-docs` endpoint returns valid OpenAPI 3.0 spec (JSON)

**After Phase 2**:
- `gradle test` passes for Auth tests
- Can login with Google/GitHub (manual test)
- `GET /api/auth/me` returns current user
- **APP IS RUNNABLE**: Frontend can start integration

**After Phase 3**:
- `gradle test` passes for Lists tests
- CRUD list endpoints working
- Authorization blocking cross-user access
- Deletion returns 409 if list has tasks

**After Phase 4**:
- `gradle test` passes for Tasks tests
- Full CRUD working
- Search and filters working
- Seed endpoint creates sample data
- **CORE MVP COMPLETE**: All features working

**After Phase 5**:
- `gradle test` passes for WebSocket tests
- WebSocket client can connect
- Overdue/due-soon notifications sent
- Client subscribe/ack messages handled

**After Phase 6**:
- `gradle build && gradle test` succeeds with 70%+ coverage
- README complete and accurate
- `.env.example` provided
- End-to-end manual tests pass

---

## Parallel Work Opportunities

- **UI Team**: After Phase 2 (Auth), can build login page, sidebar, main layout
- **Backend Team**: Phase 3-5 can work independently in parallel once Phase 2 completes
- **Dev Ops**: Can set up Docker/deployment while backend is in Phase 4-5
- **Docs**: Can draft README skeleton while implementation is ongoing

---

## Summary

**Total files to create**: ~45 files

**Key Phases**:
```
Phase 1 (Shared)    → Phase 2 (Auth) → Phase 3 (Lists) → Phase 4 (Tasks) → Phase 5 (WebSocket) → Phase 6 (Polish)
    [COMPLETE]          [IN PROGRESS]     [PENDING]        [PENDING]          [PENDING]           [PENDING]
```

**After Phase 2**: User can OAuth login, see profile
**After Phase 4**: User can manage tasks across lists
**After Phase 5**: User receives real-time notifications
**After Phase 6**: Production-ready backend deployed

**Build & Test Commands**:
```bash
# Build
gradle build

# Run tests
gradle test

# Start backend
gradle bootRun

# View coverage
gradle test jacocoTestReport
```

---

## Key Directories (using `com.taskmanager` package)

```
backend/
├── build.gradle ✓
├── src/main/java/com/taskmanager/
│   ├── TaskmanagerApplication.java ✓
│   ├── config/ (SecurityConfig, WebSocketConfig)
│   ├── controller/ (Auth, TaskList, Task, Seed)
│   ├── service/ (User, TaskList, Task, Auth)
│   ├── entity/ (User, UserAuthProvider, TaskList, Task, enums)
│   ├── repository/ (User, UserAuthProvider, TaskList, Task)
│   ├── dto/ (UserDTO, TaskListDTO, TaskDTO, requests/responses)
│   ├── websocket/ (TaskNotificationHandler, NotificationScheduler)
│   ├── security/ (OAuth2UserService, SecurityUser)
│   └── exception/ (GlobalExceptionHandler, custom exceptions)
├── src/main/resources/
│   ├── application.properties ✓
│   ├── application-dev.properties ✓
│   └── db/migration/ (V001, V002) ✓
├── src/test/java/com/taskmanager/
│   ├── service/ (UserService, TaskListService, TaskService tests)
│   ├── controller/ (Auth, TaskList, Task controller tests)
│   ├── websocket/ (WebSocket handler tests)
│   └── util/ (TestDataBuilder) ✓
└── .env.example ⏳
```
