# Project Specification: Personal To-Do Manager

## 1. Overview

A full-stack web application for managing personal tasks across multiple projects with real-time notifications. Single-user per session with SSO authentication.

## 2. Core Principles

- **Multi-user support**: Each user sees only their own data (isolated by provider + provider_user_id)
- **No data sharing**: No invites, public links, or shared lists
- **Real-time communication**: WebSocket-based notifications for task urgency

## 3. Authentication & Authorization

### 3.1 OAuth2 SSO
- **Providers**: Google OAuth, GitHub OAuth
- **Multi-Provider Support**: Single user can link multiple OAuth providers to one email
- **User Identified By**: Email address (unique per person)
- **OAuth Provider Data**: Stored separately to allow multiple provider-user mappings per user

### 3.2 Session Management
- Session must persist across page refresh
- Logout support required
- All API endpoints enforce ownership (user can only access their own data)

## 4. Data Model

### 4.1 User
```
id (PK, UUID)
email (UNIQUE, identifies the person)
display_name
avatar_url
created_at
updated_at
```

### 4.2 User Auth Provider
```
id (PK, UUID)
user_id (FK → User)
provider (enum: google, github)
provider_user_id (unique per provider)
created_at
COMPOSITE UNIQUE: (provider, provider_user_id)
COMPOSITE UNIQUE: (user_id, provider) - one provider per user
```

### 4.3 List (Project)
```
id (PK, UUID)
user_id (FK → User)
title
created_at
updated_at
```

### 4.4 Task
```
id (PK, UUID)
list_id (FK → List)
user_id (FK → User, for efficient queries)
title
description
status (enum: TO_DO, IN_PROGRESS, DONE)
priority (enum: LOW, MEDIUM, HIGH)
due_date (nullable)
created_at
updated_at
```

## 5. API Endpoints

### 5.1 Authentication
- `GET /oauth2/authorization/google` - Initiate Google OAuth
- `GET /oauth2/authorization/github` - Initiate GitHub OAuth
- `POST /logout` - Clear session
- `GET /api/auth/me` - Get current user profile

### 5.2 Lists (Projects)
- `GET /api/lists` - List all user's lists
- `POST /api/lists` - Create new list
- `PUT /api/lists/{id}` - Update list name
- `DELETE /api/lists/{id}` - Delete list (choose: cascade or block-if-not-empty)

### 5.3 Tasks
- `GET /api/tasks?list_id={id}&status={status}&priority={priority}&due_date={filter}` - List tasks with filters
- `GET /api/tasks/search?q={query}` - Search tasks by title/description
- `POST /api/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Quick status change
- `DELETE /api/tasks/{id}` - Delete task

### 5.4 Sample Data
- `POST /api/seed` - Populate database with sample data (or use Flyway seed script)

## 6. Search & Filters

### 6.1 Search
- Search across `title` and `description` fields
- Case-insensitive substring match

### 6.2 Filters
- **Status**: TO_DO, IN_PROGRESS, DONE (multi-select)
- **Priority**: LOW, MEDIUM, HIGH (multi-select)
- **Due Date**:
  - `overdue` - due_date < today AND status ≠ DONE
  - `today` - due_date = today AND status ≠ DONE
  - `next_7_days` - today < due_date ≤ today + 7 days AND status ≠ DONE
  - `all` - no date filter

## 7. WebSocket Notifications

### 7.1 Connection Flow
1. Client connects to WebSocket endpoint: `ws://server/api/ws`
2. Server sends initial batch of overdue + due-soon tasks
3. Client can send `subscribe` message to customize interval
4. Periodic checks run every N seconds (default: 60s, configurable)

### 7.2 Server → Client Messages
```json
{
  "type": "notification",
  "id": "uuid",
  "taskId": "task-uuid",
  "title": "Task title",
  "reason": "overdue" | "due-soon",
  "due_date": "2026-05-20"
}
```

### 7.3 Client → Server Messages
```json
{
  "type": "subscribe",
  "interval": 60000
}
```

```json
{
  "type": "ack",
  "notificationId": "uuid"
}
```

### 7.4 Notification Rules
- **Overdue**: status ≠ DONE AND due_date < today
- **Due Soon**: status ≠ DONE AND due_date within next 3 days (inclusive)

## 8. UI Requirements

### 8.1 Layout
- Auth screen with "Continue with Google" + "Continue with GitHub" buttons
- Main layout:
  - Sidebar: List of user's projects/lists (collapsible on mobile)
  - Main area: Task list for selected list
  - Search + Filter bar
  - Notifications panel/toast

### 8.2 Task Operations
- Create task (modal/drawer/page)
- Edit task (modal/drawer/page)
- Delete task (with confirmation)
- Quick status change from list view

### 8.3 Modern UI Standards
- Consistent spacing & typography
- Hover and focus states
- Empty states (no tasks, no search results)
- Loading states during API calls
- Client-side form validation feedback
- Responsive: works at 320px, 768px, 1440px breakpoints

## 9. Testing Strategy

### 9.1 Backend Tests
- SSO login success (mock provider)
- List CRUD + authorization
- Task CRUD + authorization
- Cross-user isolation (user A cannot see user B's data)
- WebSocket notification dispatch (overdue/due-soon)
- Input validation

### 9.2 Frontend Tests (Vitest + @vue/test-utils)
- Form validation (required fields, date picker)
- List rendering
- Search/filter logic
- Empty states
- Loading states
- Auth state management

### 9.3 E2E Tests (Playwright)
- Complete OAuth login flow
- Create list → create task → update task → delete task
- Search finds tasks, filters work
- WebSocket receives notifications
- Responsive layout
- Cross-user isolation

## 10. Database Setup

### 10.1 MySQL 8.x
- Host: localhost (default)
- Port: 3306 (default)
- Database: `task_manager`
- User: `taskmanager` (or root for dev)

### 10.2 Migrations (Flyway)
- Location: `src/main/resources/db/migration/`
- Naming: `V001__initial_schema.sql`, `V002__add_column.sql`
- Auto-runs on app startup
- Config in `application.properties`:
  ```properties
  spring.flyway.locations=classpath:db/migration
  spring.flyway.baselineOnMigrate=true
  ```

### 10.3 Seed Data
- Option A: SQL migration `V999__seed_data.sql`
- Option B: REST endpoint `POST /api/seed`
- Option C: Manual documentation

## 11. Deletion Behavior

**Block Until Empty** (no database cascade):
- Cannot delete list if it contains tasks → returns 409 Conflict
- User must delete all tasks first, then delete the list
- Deletion logic enforced in application layer (service)
- Prevents accidental data loss
- No database-level CASCADE; all constraint logic in Java code

## 12. Error Handling

### 12.1 HTTP Status Codes
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Validation failed
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Authenticated but no access
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### 12.2 Error Response Format
```json
{
  "error": "Error message",
  "details": { "field": "error details" }
}
```

## 13. Security Requirements

- OAuth2 secure redirect + token exchange
- HTTPS in production
- CSRF protection on state-changing endpoints
- SQL injection prevention (use JPA parameterized queries)
- XSS prevention (Vue auto-escapes, validate server-side)
- Rate limiting (optional but recommended)
- Secrets in environment variables (.env), never committed

## 14. Deliverables

- [ ] Source code in Git repository
- [ ] README.md:
  - Local setup (Java 21, MySQL, Node.js)
  - OAuth provider setup (Google + GitHub)
  - WebSocket message format documentation
  - Notification rules explanation
  - API documentation (OpenAPI/Swagger or README)
  - Deletion behavior choice
  - Sample data setup instructions
- [ ] Tests passing locally
- [ ] Docker/docker-compose (optional or documented as not applicable)

## 15. Acceptance Criteria

- User can log in via Google OAuth
- User can log in via GitHub OAuth
- User can create lists
- User can create tasks with all fields
- User can search tasks by title/description
- User can filter by status, priority, due date
- Data is private per user (cross-user isolation verified)
- Real-time WebSocket notifications work (overdue + due-soon)
- WebSocket includes client → server message (subscribe/ack)
- Tests pass locally (`gradle test`, `npm test`, `npm run test:e2e`)
- README is complete and accurate
