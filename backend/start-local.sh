#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting Task Manager Backend (Local)${NC}"

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java not found. Please install Java 21.${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | awk -F'"' '{print $2}')
echo -e "${GREEN}✓ Java $JAVA_VERSION found${NC}"

if ! command -v mysql &> /dev/null; then
    echo -e "${YELLOW}⚠ MySQL client not found (optional, for manual testing)${NC}"
fi

# Load environment variables from .env if it exists
if [ -f "$(dirname "$0")/.env" ]; then
    echo "Loading .env configuration..."
    export $(cat "$(dirname "$0")/.env" | grep -v '^#' | xargs)
else
    echo -e "${YELLOW}⚠ .env file not found, using defaults${NC}"
    export SPRING_PROFILES_ACTIVE=dev
    export SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/task_manager
    export SPRING_DATASOURCE_USERNAME=root
    export SPRING_DATASOURCE_PASSWORD=root
fi

# Verify database connection
echo "Verifying MySQL connection..."
if ! mysql -h ${DB_HOST:-127.0.0.1} -u ${SPRING_DATASOURCE_USERNAME}  -P ${MYSQL_PORT} -D ${MYSQL_DATABASE} -p${SPRING_DATASOURCE_PASSWORD} -e "SELECT 1" &> /dev/null; then
    echo "${DB_HOST:-localhost} -u ${SPRING_DATASOURCE_USERNAME}  -P ${MYSQL_PORT} -D ${MYSQL_DATABASE} -p${SPRING_DATASOURCE_PASSWORD}"    
    echo -e "${RED}✗ Cannot connect to MySQL at ${DB_HOST:-localhost}:${MYSQL_PORT:-3306}${NC}"
    echo "Make sure MySQL is running. For Docker: docker compose up -d mysql"
    exit 1
fi
echo -e "${GREEN}✓ MySQL connection verified${NC}"

# Build and run
echo -e "\n${YELLOW}Building and starting Spring Boot application...${NC}\n"
cd "$(dirname "$0")"
./gradlew bootRun --args='--spring.profiles.active=dev'
