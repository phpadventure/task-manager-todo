#!/bin/bash

# Database initialization script with dynamic database name
# Uses MYSQL_DATABASE environment variable (default: task_manager)
# Executed automatically by MySQL container on startup

DB_NAME="${MYSQL_DATABASE:-task_manager}"

mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\`
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
EOF

echo "Database '$DB_NAME' created successfully"
