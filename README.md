# claude-code-task-manager

Personal To-Do Manager web application with full-stack implementation: Spring Boot backend, Vue 3 frontend, MySQL database, OAuth2 authentication, and real-time WebSocket notifications.

## ✨ Features

### Core Features
- **OAuth2 SSO** - Single sign-on with Google & GitHub
- **Task Lists** - Organize tasks into multiple projects/lists
- **Task Management** - Create, read, update, delete tasks with full CRUD
- **Task Attributes** - Title, description, status (To Do / In Progress / Done), priority (Low / Medium / High), due date
- **Search & Filters** - Search by title/description, filter by status/priority/due date, find overdue tasks
- **Real-time Notifications** - WebSocket push for overdue and due-soon tasks
- **Session Persistence** - Stay logged in across page refresh
- **Authorization** - Declarative @PreAuthorize security with cross-user data isolation

### Backend Quality
- **130+ Unit Tests** - Comprehensive test coverage for services and controllers
- **Authorization Testing** - 100% coverage of cross-user isolation
- **Code Quality** - Google Java Style compliance via Checkstyle
- **OpenAPI Documentation** - Interactive Swagger UI at `/swagger-ui.html`
- **Error Handling** - Centralized exception handling with consistent error responses

---

## Prerequisites

### Local Development (No Docker)
- **Java 21**: `sdk install java 21.0.11-oracle` (via SDKMan)
- **Node.js & npm**: `brew install node`
- **MySQL 8.x**: Running locally or via Docker

### Using Docker
- **Docker & Docker Compose**: [Install Docker Desktop](https://www.docker.com/products/docker-desktop)

## Authentication: Session-Based Cookies

This app uses **Spring Session with HttpOnly cookies** (not JWT). Session cookie persists across page refresh automatically.

**How it works**:
1. User clicks "Login with Google/GitHub"
2. Backend exchanges OAuth code for user info, creates session
3. Browser receives HttpOnly, Secure session cookie
4. Page refresh: Cookie sent automatically → user stays logged in ✅

**Security**: Token never exposed to JavaScript (XSS protection).

---

## Getting Started

### Option 1: Local Development (Recommended for feature work)

**Setup** (one-time):
1. Edit `/etc/hosts` and add:
   ```
   127.0.0.1 app.localhost
   127.0.0.1 api.localhost
   ```

2. Start MySQL (if not already running):
   ```bash
   # macOS
   brew services start mysql
   # Linux
   sudo systemctl start mysql
   ```

3. Copy environment:
   ```bash
   cp .env.example .env
   ```

**Startup** (each time):
```bash
# Terminal 1: Backend on http://api.localhost:8080
cd backend
./gradlew bootRun

# Terminal 2: Frontend on http://app.localhost:5173
cd ui
npm install  # First time only
npm run dev
```

**Access**: Open `http://app.localhost:5173` in browser

**Session cookies work**:
- Vite dev server proxies `/api` to backend
- Cookie domain rewritten to `.localhost` (covers both subdomains)
- Page refresh: user stays logged in ✅

---

### Option 2: Full Stack with Docker (For production-like testing)

**Setup**:
```bash
# Copy environment configuration
cp .env.example .env

# Start all services (MySQL + Backend + Frontend)
docker-compose up -d

# Verify services are running
docker-compose ps
```

**Access**: `http://localhost` (via Nginx reverse proxy)

**View logs**:
```bash
docker-compose logs -f backend    # Backend logs
docker-compose logs -f mysql      # MySQL logs
```

**Session cookies work**:
- Nginx reverse proxy on single origin
- Frontend and backend communication through Nginx
- Cookie domain: `localhost`

**Stop services**:
```bash
docker-compose down
```

## Verify Connections

### Check Docker Services Status
```bash
docker-compose ps
```
Expected output: Both `mysql` and `backend` services should be `Up` and healthy.

### Test Backend Health
```bash
# From host machine
curl http://localhost:8080/api/actuator/health

# From Docker network (inside backend container)
docker-compose exec backend curl http://localhost:8080/api/actuator/health
```

### Test MySQL Connectivity from Backend
```bash
# Verify backend can reach MySQL
docker-compose exec backend curl -v mysql:3306

# Should hang or timeout (expected - MySQL port 3306 is not HTTP)
# Connection refused = MySQL not reachable (problem)
```

### View Real-time Logs
```bash
# Follow backend logs
docker-compose logs -f backend

# Follow MySQL logs
docker-compose logs -f mysql

# Follow both
docker-compose logs -f
```

### Stop All Services
```bash
docker-compose down
```

### Rebuild Backend (after code changes)
```bash
docker-compose up -d --build backend
```

## Code Quality

### Code Formatting & Linting

**Frontend**:
- **Prettier**: Automatic code formatting (semicolons, quotes, indentation)
- **ESLint**: Code quality checks and Vue 3 best practices

Format and lint code before committing:
```bash
cd ui
npm run format      # Format all files
npm run lint        # Fix linting issues
```

## Development

### Build Commands

**Backend (Gradle):**
```bash
cd backend
gradle build              # Full build with tests
gradle test               # Run tests only
gradle checkstyleMain     # Check code style (Google Java Style)
gradle jacocoTestReport   # Code coverage report (excludes DTOs, Entities, Config, Exceptions)
gradle bootRun            # Local dev (uses start-local.sh wrapper)
```

**Frontend (Vue):**
```bash
cd ui
npm run dev         # Development server with hot reload
npm run build       # Production build
npm run lint        # ESLint with auto-fix
npm run format      # Format code with Prettier
npm run test        # Unit tests
```

### Database Setup

**Database Creation (Docker):**
- The `task_manager` database is automatically created when the MySQL container starts
- Initialization script: `docker/init-db/01-create-database.sql` (runs before Java backend container)
- The Java backend does NOT create the database—it only validates existing schema and applies migrations

**Database Migrations:**
- Flyway automatically runs migrations on Spring Boot startup
- Migrations are in `backend/src/main/resources/db/migration/`

## API Documentation

Complete API reference with examples for all endpoints:
- **[API.md](./API.md)** - Full API documentation (Lists, Tasks, Query examples)
- **Swagger UI** - Interactive API docs at http://localhost:8080/swagger-ui.html
- **OpenAPI Spec** - Machine-readable spec at http://localhost:8080/api-docs

### Authorization

All endpoints use **Spring Security's `@PreAuthorize` annotation** with custom expression handlers for declarative authorization:

**Security Implementation**:
- `SecurityService` provides ownership check methods for SpEL expressions
- Edit/delete endpoints protected with `@PreAuthorize("@securityService.userOwns...")`
- Cross-user data isolation enforced (returns 403 Forbidden if user doesn't own resource)
- Defense-in-depth: @PreAuthorize at controller + service layer checks

**Protected Endpoints** (403 Forbidden if unauthorized):
- `PUT /api/lists/{id}` - Update list (user must own list)
- `DELETE /api/lists/{id}` - Delete list (user must own list)
- `PUT /api/lists/{listId}/tasks/{taskId}` - Update task (user must own list + task)
- `DELETE /api/lists/{listId}/tasks/{taskId}` - Delete task (user must own list + task)
- `PATCH /api/tasks/{taskId}/status` - Update task status (user must own task)

### Task Lists & Tasks

**List CRUD**:
- `GET /api/lists` - Get all user's lists
- `POST /api/lists` - Create new list
- `PUT /api/lists/{id}` - Update list title
- `DELETE /api/lists/{id}` - Delete list (409 Conflict if list has tasks)

**Task CRUD** (nested under list):
- `GET /api/lists/{listId}/tasks` - Get all tasks in list
- `POST /api/lists/{listId}/tasks` - Create task in list
- `PUT /api/lists/{listId}/tasks/{taskId}` - Update task details
- `DELETE /api/lists/{listId}/tasks/{taskId}` - Delete task
- `PATCH /api/tasks/{taskId}/status` - Quick status update

**Task Search & Filters** (user-wide):
- `GET /api/tasks` - Get all user's tasks
- `GET /api/tasks?status=TO_DO&priority=HIGH` - Filter by status/priority
- `GET /api/tasks/search?q=query` - Search title & description
- `GET /api/tasks/overdue` - Get overdue tasks (due_date < today, status ≠ DONE)
- `GET /api/tasks/due-soon` - Get tasks due within 3 days

For complete request/response examples, see **[API.md](./API.md)**.

### Deletion Behavior

**List Deletion**: Block pattern - cannot delete list with active tasks
- Returns `409 Conflict` if list contains any tasks
- User must delete all tasks first, then delete the list
- Prevents accidental data loss

**Task Deletion**: Immediate hard delete
- No soft delete or archive feature
- Deleted tasks are permanently removed from database

### Sample Data & Seed Endpoint

Generate sample data for testing:
```bash
# Seed database with sample lists and tasks
curl -X POST http://localhost:8080/api/seed \
  -H "Content-Type: application/json" \
  -d '{"userId": "your-user-id-here"}'
```

Returns:
- 3 sample task lists (Personal, Work, Shopping)
- 10 sample tasks with various statuses and priorities
- All data scoped to the specified user

---

## OAuth2 Authentication Setup

### Google OAuth

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable OAuth 2.0:
   - Go to **APIs & Services > Credentials**
   - Click **Create Credentials > OAuth client ID**
   - Application type: **Web Application**
   - Authorized redirect URIs: `http://localhost:8080/api/login/oauth2/code/google`
4. Copy **Client ID** and **Client Secret**
5. Add to `.env`:
   ```
   GOOGLE_CLIENT_ID=your-client-id
   GOOGLE_CLIENT_SECRET=your-client-secret
   ```

### GitHub OAuth

1. Go to GitHub Settings > [Developer settings > OAuth Apps](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Fill in:
   - **Application name**: `Task Manager`
   - **Homepage URL**: `http://localhost:8080`
   - **Authorization callback URL**: `http://localhost:8080/api/login/oauth2/code/github`
4. Copy **Client ID** and **Client Secret**
5. Add to `.env`:
   ```
   GITHUB_CLIENT_ID=your-client-id
   GITHUB_CLIENT_SECRET=your-client-secret
   ```

#### GitHub Private Email Handling

GitHub users with private email settings require special handling:

- **Scope**: The application requests `user:email` scope (allows access to primary email)
- **Email Resolution**:
  1. Standard OAuth `/user` endpoint is called first
  2. If email is null (private setting), backend calls GitHub's `/user/emails` endpoint
  3. Backend retrieves the user's **primary verified email** from the secondary endpoint
- **Error Handling**: If email cannot be retrieved after both attempts, authentication is rejected with a clear error message:
  > "Unable to retrieve email from GitHub. Please ensure your email is verified and not private in GitHub settings."

**To enable login with private email**: Users must either:
- Make their email public in [GitHub email settings](https://github.com/settings/emails)
- Or keep email private but ensure it's verified (backend will fetch it via secondary API)

### Multi-Provider Linking

Users can link multiple OAuth providers to a single account:
- First login creates a user record with the provider
- Subsequent logins with a different provider linking the same email address automatically link providers
- User can authenticate with either Google or GitHub after linking

---

## WebSocket Notifications

### Connection

Client connects via WebSocket to `/api/ws` using STOMP protocol:

```javascript
const client = new StompClient();
client.connect({}, () => {
  client.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    console.log('Notification:', notification);
  });
});
```

### Subscribe Message

After connecting, client sends subscription interval (milliseconds):

```javascript
client.send('/app/subscribe', {}, JSON.stringify({ interval: 60000 }));
```

- **interval**: Time in milliseconds between notification batches (default: 60000ms = 1 minute)
- Scheduler runs every 10 seconds internally but respects per-user interval

### Notification Message Format

Server sends notifications to connected clients:

```json
{
  "type": "notification",
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "taskId": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Buy groceries",
  "reason": "overdue",
  "dueDate": "2026-05-20",
  "priority": "HIGH",
  "sentAt": "2026-05-21T10:30:00Z"
}
```

**Fields**:
- `reason`: "overdue" (due_date < today) or "due-soon" (due within 3 days)
- `priority`: "LOW", "MEDIUM", or "HIGH"

### Acknowledgement Message

Client acknowledges receipt of notification:

```javascript
client.send('/app/ack', {}, JSON.stringify({
  type: 'ack',
  notificationId: '550e8400-e29b-41d4-a716-446655440000'
}));
```

### Notification Rules

Overdue & Due-Soon logic runs every 10 seconds (configurable):
- **Overdue**: `status ≠ DONE AND due_date < today`
- **Due Soon**: `status ≠ DONE AND due_date within next 3 days (inclusive)`

Each user receives notifications based on their configured interval:
- Default: 60 seconds (configurable via subscribe message)
- Deduplication: Same task/reason not sent twice in same cycle

### Configuration

Backend properties (in `application.properties`):
```properties
# WebSocket CORS origins
app.websocket.allowed-origins=http://app.localhost:5173,http://localhost:5173

# Scheduler interval (ms) - runs every 10 seconds
app.notification.interval=10000
```

### Manual Testing

For detailed manual testing procedures to verify WebSocket functionality:
- **[backend/verification/PHASE5_MANUAL_TESTS.md](./backend/verification/PHASE5_MANUAL_TESTS.md)** - Step-by-step WebSocket verification guide

---

## Testing

### Backend Tests
```bash
cd backend
./gradlew test                    # Run all tests
./gradlew test --tests "*GitHub*" # Test GitHub OAuth functionality
./gradlew test --tests "*Notification*" # WebSocket notification tests
```

### Frontend Tests
```bash
cd ui
npm run test        # Unit tests (Vitest)
npx playwright test # E2E tests
```
