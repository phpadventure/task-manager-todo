# Starting Backend Locally (Without Docker)

## Prerequisites
- Java 21+ installed
- MySQL 8.x running locally or via Docker
- `.env` file configured (copy from `.env.example`)

## Option 1: MySQL via Docker (Recommended)
```bash
# Start MySQL container only
docker compose up -d mysql

# Start backend locally
./backend/start-local.sh
```

## Option 2: MySQL Installed Locally
Ensure MySQL is running:
```bash
# macOS (Homebrew)
brew services start mysql

# Linux (systemd)
sudo systemctl start mysql

# Then start backend
./backend/start-local.sh
```

## Manual Start (No Script)
```bash
cd backend
export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/task_manager
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root

./gradlew bootRun
```

## What the Script Does
1. ✓ Checks Java 21 is installed
2. ✓ Loads `.env` configuration
3. ✓ Verifies MySQL connectivity
4. ✓ Builds and starts Spring Boot with `dev` profile

## Access Backend
- API Base: `http://localhost:8080/api`
- Health: `http://localhost:8080/api/actuator/health` (requires actuator dependency)

## Troubleshooting
- **MySQL connection refused**: Check MySQL is running and credentials in `.env` match
- **Port 8080 in use**: Kill process: `lsof -ti:8080 | xargs kill -9`
- **Gradle build fails**: Run `./gradlew clean build` first
