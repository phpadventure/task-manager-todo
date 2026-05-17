# claude-code-task-manager

Personal To-Do Manager web application with full-stack implementation: Spring Boot backend, Vue 3 frontend, MySQL database, OAuth2 authentication, and real-time WebSocket notifications.

## Prerequisites

### Local Development (No Docker)
- **Java 21**: `sdk install java 21.0.11-oracle` (via SDKMan)
- **Node.js & npm**: `brew install node`
- **MySQL 8.x**: Running locally or via Docker

### Using Docker
- **Docker & Docker Compose**: [Install Docker Desktop](https://www.docker.com/products/docker-desktop)

## Getting Started

### Option 1: Full Stack with Docker (Recommended for quick setup)

```bash
# Copy environment configuration
cp .env.example .env

# Start all services (MySQL + Backend Java + Frontend Vue)
docker-compose up -d

# Verify services are running
docker-compose ps
```

**Access the application:**
- Frontend: `http://localhost:5173` (Vue dev server)
- Backend API: `http://localhost:8080/api`

**View logs:**
```bash
docker-compose logs -f backend    # Java backend logs
docker-compose logs -f mysql      # MySQL logs
```

### Option 2: Everything Local (Java + Node + MySQL)

```bash
# Start MySQL locally (if not already running)
# macOS: brew services start mysql
# Linux: sudo systemctl start mysql

# Copy environment and update datasource if needed
cp .env.example .env

# Terminal 1: Start backend
cd backend
./start-local.sh

# Terminal 2: Start frontend
cd frontend
npm install
npm run dev
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

## Development

### Build Commands

**Backend (Gradle):**
```bash
cd backend
gradle build              # Full build with tests
gradle test               # Run tests only
gradle checkstyleMain     # Check code style
gradle bootRun            # Local dev (uses start-local.sh wrapper)
```

**Frontend (Vue):**
```bash
cd frontend
npm run dev         # Development server with hot reload
npm run build       # Production build
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
