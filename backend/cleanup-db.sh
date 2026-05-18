#!/bin/bash

# Load environment variables
source ./.env

# Default values
DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${MYSQL_PORT:-3307}
DB_NAME=${MYSQL_DATABASE:-task_manager}
DB_USER=${MYSQL_USER:-taskuser}
DB_PASSWORD=${MYSQL_PASSWORD:-taskpass}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root}

echo "🗑️  Cleaning up database..."
echo "Database: $DB_NAME on $DB_HOST:$DB_PORT"

# Drop and recreate database
mysql -h "$DB_HOST" -P "$DB_PORT" -u root -p"$MYSQL_ROOT_PASSWORD" <<EOF
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Database cleaned and recreated successfully"
    echo "📋 Running migrations..."
    ./gradlew clean build -q && echo "✅ Migrations applied"
else
    echo "❌ Failed to clean database"
    exit 1
fi
