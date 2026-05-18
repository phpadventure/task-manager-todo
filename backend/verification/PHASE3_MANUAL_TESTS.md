# Phase 3: Lists Feature - Manual Verification Guide

This guide provides step-by-step instructions to manually verify Phase 3 (Lists feature) is working correctly.

## Prerequisites

1. Backend running: `./gradlew bootRun` (listening on `http://localhost:8080`)
2. Authenticated user (from Phase 2)
3. `curl` command available or use Swagger UI at `http://localhost:8080/swagger-ui.html`

## Swagger UI Verification (Easiest)

1. Open `http://localhost:8080/swagger-ui.html`
2. Expand "Lists" section to see all endpoints
3. Follow the test cases below using "Try it out" button

---

## Test Case 1: Create a List

### Using Swagger UI
1. Navigate to `POST /api/lists`
2. Click "Try it out"
3. Enter request body:
   ```json
   {
     "title": "Groceries"
   }
   ```
4. Click "Execute"
5. **Expected**: Status `201 Created` with response containing `id`, `title`, `created_at`, `updated_at`

### Using cURL
```bash
curl -X POST http://localhost:8080/api/lists \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>" \
  -d '{"title":"Groceries"}'
```

**Expected Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Groceries",
  "created_at": "2026-05-18T22:00:00Z",
  "updated_at": "2026-05-18T22:00:00Z"
}
```

---

## Test Case 2: Get All Lists

### Using Swagger UI
1. Navigate to `GET /api/lists`
2. Click "Try it out"
3. Click "Execute"
4. **Expected**: Status `200 OK` with array of lists

### Using cURL
```bash
curl -X GET http://localhost:8080/api/lists \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>"
```

**Expected Response** (200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Groceries",
    "created_at": "2026-05-18T22:00:00Z",
    "updated_at": "2026-05-18T22:00:00Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Work",
    "created_at": "2026-05-18T22:05:00Z",
    "updated_at": "2026-05-18T22:05:00Z"
  }
]
```

---

## Test Case 3: Get Single List by ID

### Using Swagger UI
1. Navigate to `GET /api/lists/{id}`
2. Click "Try it out"
3. Enter the list ID from Test Case 1
4. Click "Execute"
5. **Expected**: Status `200 OK` with single list object

### Using cURL
```bash
curl -X GET http://localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>"
```

**Expected Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Groceries",
  "created_at": "2026-05-18T22:00:00Z",
  "updated_at": "2026-05-18T22:00:00Z"
}
```

---

## Test Case 4: Update List Title

### Using Swagger UI
1. Navigate to `PUT /api/lists/{id}`
2. Click "Try it out"
3. Enter list ID from Test Case 1
4. Enter request body:
   ```json
   {
     "title": "Groceries & Supplies"
   }
   ```
5. Click "Execute"
6. **Expected**: Status `200 OK` with updated `title` and new `updated_at`

### Using cURL
```bash
curl -X PUT http://localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>" \
  -d '{"title":"Groceries & Supplies"}'
```

**Expected Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Groceries & Supplies",
  "created_at": "2026-05-18T22:00:00Z",
  "updated_at": "2026-05-18T22:30:00Z"
}
```

---

## Test Case 5: Delete Empty List

### Using Swagger UI
1. Navigate to `DELETE /api/lists/{id}`
2. Click "Try it out"
3. Enter list ID from Test Case 1 (or create a new empty list)
4. Click "Execute"
5. **Expected**: Status `204 No Content` (empty response body)

### Using cURL
```bash
curl -X DELETE http://localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>"
```

**Expected Response** (204 No Content):
- No response body

---

## Test Case 6: Validation Error - Empty Title

### Using Swagger UI
1. Navigate to `POST /api/lists`
2. Click "Try it out"
3. Enter request body with blank title:
   ```json
   {
     "title": ""
   }
   ```
4. Click "Execute"
5. **Expected**: Status `400 Bad Request` with validation error

### Using cURL
```bash
curl -X POST http://localhost:8080/api/lists \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>" \
  -d '{"title":""}'
```

**Expected Response** (400 Bad Request):
```json
{
  "error": "Validation failed",
  "details": {
    "field": "title",
    "message": "Title cannot be blank"
  },
  "timestamp": "2026-05-18T22:35:00Z"
}
```

---

## Test Case 7: Authorization Error - 403 Forbidden

This test requires 2 users. Manual verification is complex. **Tests cover this case.**

**Conceptual flow** (tested in unit tests):
1. User A creates a list
2. User B attempts to access/update/delete User A's list
3. **Expected**: Status `403 Forbidden` with error message

---

## Test Case 8: Not Found Error - 404

### Using Swagger UI
1. Navigate to `GET /api/lists/{id}`
2. Click "Try it out"
3. Enter a non-existent list ID (e.g., `550e8400-0000-0000-0000-000000000000`)
4. Click "Execute"
5. **Expected**: Status `404 Not Found` with error message

### Using cURL
```bash
curl -X GET http://localhost:8080/api/lists/550e8400-0000-0000-0000-000000000000 \
  -H "Content-Type: application/json" \
  -b "JSESSIONID=<your_session_id>"
```

**Expected Response** (404 Not Found):
```json
{
  "error": "Resource not found",
  "details": {
    "resource": "TaskList",
    "id": "550e8400-0000-0000-0000-000000000000"
  },
  "timestamp": "2026-05-18T22:35:00Z"
}
```

---

## Test Case 9: Conflict Error - Cannot Delete List with Tasks

**Note**: This test requires Phase 4 (Tasks) to be implemented. For now, **skip or verify tests passing**.

### Expected behavior (tested in tests):
1. Create a list
2. Create a task in that list (Phase 4)
3. Attempt to delete the list
4. **Expected**: Status `409 Conflict` with error message

---

## Session Cookie Verification

To verify session persistence is working:

1. Get session ID from login response
2. Make request to `GET /api/lists` with the session cookie
3. Refresh page / make another request
4. Session should remain valid across requests

### Extract session ID (from browser DevTools):
1. Open DevTools (F12)
2. Go to "Application" → "Cookies"
3. Look for `JSESSIONID` cookie
4. Use in curl commands: `-b "JSESSIONID=<value>"`

---

## Quick Verification Checklist

- [ ] POST /api/lists creates list (201)
- [ ] GET /api/lists returns all user lists (200)
- [ ] GET /api/lists/{id} returns single list (200)
- [ ] PUT /api/lists/{id} updates title (200)
- [ ] DELETE /api/lists/{id} deletes empty list (204)
- [ ] POST /api/lists with blank title returns 400
- [ ] GET /api/lists/{id} with invalid ID returns 404
- [ ] Swagger UI accessible at /swagger-ui.html
- [ ] All tests passing: `./gradlew test --tests "*TaskList*"`

---

## Troubleshooting

### 401 Unauthorized
- You're not logged in
- **Fix**: Complete Phase 2 (Auth) OAuth login first
- Verify session cookie is present: `curl -b "JSESSIONID=..." ...`

### 404 Not Found (unexpected)
- List ID doesn't exist
- **Fix**: Use list ID from GET /api/lists response

### 409 Conflict (on DELETE)
- List still has tasks
- **Fix**: Delete all tasks first, then delete list (Phase 4 behavior)

### CORS Error
- Frontend making cross-origin requests without proper config
- **Fix**: Configure CORS in SecurityConfig (Phase 2 complete)

---

## Next Steps

After Phase 3 verification:
- Phase 4: Tasks Feature depends on Phase 3 lists
- Phase 5: WebSocket Notifications uses tasks
- Proceed with implementation once all manual tests pass
