# claude-code-task-manager

Personal To-Do Manager web application with full-stack implementation: Spring Boot backend, Vue 3 frontend, MySQL database, OAuth2 authentication, and real-time WebSocket notifications.

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
