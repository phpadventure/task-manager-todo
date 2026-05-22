# Task Manager API Documentation

Complete API reference for the Personal To-Do Manager backend.

---

## Task Lists (Projects) API

### GET /api/lists
Get all task lists for the authenticated user.

**Request**:
```bash
curl -X GET http://api.localhost:8080/api/lists \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Personal Projects",
    "created_at": "2026-05-20T10:30:00Z",
    "updated_at": "2026-05-20T10:30:00Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Work Tasks",
    "created_at": "2026-05-21T14:15:00Z",
    "updated_at": "2026-05-21T14:15:00Z"
  }
]
```

### POST /api/lists
Create a new task list.

**Request**:
```bash
curl -X POST http://api.localhost:8080/api/lists \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "title": "Shopping List"
  }'
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "title": "Shopping List",
  "created_at": "2026-05-22T09:00:00Z",
  "updated_at": "2026-05-22T09:00:00Z"
}
```

### PUT /api/lists/{id}
Update a task list title.

**Request**:
```bash
curl -X PUT http://api.localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440002 \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "title": "Grocery Shopping"
  }'
```

**Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "title": "Grocery Shopping",
  "created_at": "2026-05-22T09:00:00Z",
  "updated_at": "2026-05-22T10:30:00Z"
}
```

### DELETE /api/lists/{id}
Delete a task list. Returns **409 Conflict** if list contains tasks.

**Request**:
```bash
curl -X DELETE http://api.localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440002 \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (204 No Content):
```
(empty body)
```

**Error Response** (409 Conflict - list has tasks):
```json
{
  "error": "Cannot delete list with active tasks",
  "details": {
    "message": "Please delete all tasks in this list first"
  },
  "timestamp": "2026-05-22T10:30:00Z"
}
```

### Deletion Behavior
- **No Cascade Delete**: Lists cannot be deleted if they contain tasks
- **Block Pattern**: User must manually delete all tasks before deleting the list
- **Why**: Prevents accidental data loss

---

## Tasks API

### GET /api/lists/{listId}/tasks
Get all tasks in a specific list (list-scoped).

**Request**:
```bash
curl -X GET http://api.localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000/tasks \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (200 OK):
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "list_id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Buy milk",
    "description": "2% milk from Whole Foods",
    "status": "to_do",
    "priority": "medium",
    "due_date": "2026-05-25",
    "created_at": "2026-05-21T10:30:00Z",
    "updated_at": "2026-05-21T10:30:00Z"
  }
]
```

### GET /api/tasks
Get all tasks for the authenticated user (global view).

**Query Parameters**:
- `status`: Filter by status (to_do, in_progress, done)
- `priority`: Filter by priority (low, medium, high)

**Request**:
```bash
# All tasks
curl -X GET http://api.localhost:8080/api/tasks \
  -H "Cookie: JSESSIONID=your-session-id"

# Filter by status
curl -X GET "http://api.localhost:8080/api/tasks?status=to_do" \
  -H "Cookie: JSESSIONID=your-session-id"

# Filter by priority
curl -X GET "http://api.localhost:8080/api/tasks?priority=high" \
  -H "Cookie: JSESSIONID=your-session-id"

# Combined filters
curl -X GET "http://api.localhost:8080/api/tasks?status=in_progress&priority=high" \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (200 OK):
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "list_id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Buy milk",
    "description": "2% milk from Whole Foods",
    "status": "to_do",
    "priority": "medium",
    "due_date": "2026-05-25",
    "created_at": "2026-05-21T10:30:00Z",
    "updated_at": "2026-05-21T10:30:00Z"
  }
]
```

### GET /api/tasks/search
Search tasks by title or description.

**Query Parameters**:
- `q` (required): Search query

**Request**:
```bash
curl -X GET "http://api.localhost:8080/api/tasks/search?q=milk" \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (200 OK):
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "title": "Buy milk",
    "description": "2% milk from Whole Foods",
    "status": "to_do",
    "priority": "medium",
    "due_date": "2026-05-25",
    "created_at": "2026-05-21T10:30:00Z",
    "updated_at": "2026-05-21T10:30:00Z"
  }
]
```

### GET /api/tasks/overdue
Get all overdue tasks (status ≠ DONE AND due_date < today).

**Request**:
```bash
curl -X GET http://api.localhost:8080/api/tasks/overdue \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (200 OK):
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440000",
    "title": "Buy milk",
    "status": "to_do",
    "due_date": "2026-05-15",
    "created_at": "2026-05-21T10:30:00Z"
  }
]
```

### GET /api/tasks/due-soon
Get tasks due within the next 3 days (status ≠ DONE AND due_date within next 3 days inclusive).

**Request**:
```bash
curl -X GET http://api.localhost:8080/api/tasks/due-soon \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (200 OK):
```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "title": "Prepare presentation",
    "status": "in_progress",
    "due_date": "2026-05-23",
    "created_at": "2026-05-21T10:30:00Z"
  }
]
```

### POST /api/lists/{listId}/tasks
Create a new task in a list.

**Request**:
```bash
curl -X POST http://api.localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000/tasks \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "title": "Buy eggs",
    "description": "Organic, free-range eggs",
    "priority": "high",
    "due_date": "2026-05-25"
  }'
```

**Response** (201 Created):
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440002",
  "list_id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Buy eggs",
  "description": "Organic, free-range eggs",
  "status": "to_do",
  "priority": "high",
  "due_date": "2026-05-25",
  "created_at": "2026-05-22T10:30:00Z",
  "updated_at": "2026-05-22T10:30:00Z"
}
```

### PUT /api/lists/{listId}/tasks/{taskId}
Update a task.

**Request**:
```bash
curl -X PUT http://api.localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000/tasks/650e8400-e29b-41d4-a716-446655440002 \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "title": "Buy eggs and milk",
    "description": "Organic eggs and 2% milk",
    "priority": "high",
    "due_date": "2026-05-24"
  }'
```

**Response** (200 OK):
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440002",
  "list_id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Buy eggs and milk",
  "description": "Organic eggs and 2% milk",
  "status": "to_do",
  "priority": "high",
  "due_date": "2026-05-24",
  "created_at": "2026-05-22T10:30:00Z",
  "updated_at": "2026-05-22T11:00:00Z"
}
```

### PATCH /api/tasks/{taskId}/status
Quickly update task status.

**Request**:
```bash
curl -X PATCH http://api.localhost:8080/api/tasks/650e8400-e29b-41d4-a716-446655440002/status \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d '{
    "status": "done"
  }'
```

**Response** (200 OK):
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440002",
  "title": "Buy eggs and milk",
  "status": "done",
  "priority": "high",
  "due_date": "2026-05-24",
  "created_at": "2026-05-22T10:30:00Z",
  "updated_at": "2026-05-22T12:00:00Z"
}
```

**Valid status values**: `to_do`, `in_progress`, `done`

### DELETE /api/lists/{listId}/tasks/{taskId}
Delete a task.

**Request**:
```bash
curl -X DELETE http://api.localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000/tasks/650e8400-e29b-41d4-a716-446655440002 \
  -H "Cookie: JSESSIONID=your-session-id"
```

**Response** (204 No Content):
```
(empty body)
```

### Task Fields Reference

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `title` | string | ✓ | Task title (max 255 chars) |
| `description` | string | | Task details (optional) |
| `status` | string | | One of: `to_do`, `in_progress`, `done` (default: `to_do`) |
| `priority` | string | | One of: `low`, `medium`, `high` (default: `medium`) |
| `due_date` | date | | Date in YYYY-MM-DD format (optional) |

### Task Status Values
- `to_do` - New task, not started
- `in_progress` - Currently being worked on
- `done` - Completed

### Task Priority Values
- `low` - Low priority
- `medium` - Normal priority (default)
- `high` - High priority

---

## Swagger/OpenAPI Documentation

Full interactive API documentation is available:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec (JSON)**: http://localhost:8080/api-docs

The Swagger UI provides:
- Interactive endpoint testing
- Request/response schemas
- Authentication examples
- Error response documentation
