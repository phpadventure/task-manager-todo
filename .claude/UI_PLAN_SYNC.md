# UI_PLAN.MD Sync Report: Backend → Frontend Alignment

**Date**: 2026-05-23  
**Status**: Backend 100% Complete | UI Plan needs critical updates

---

## Executive Summary

The backend is **production-ready** with all 6 phases complete. The UI_PLAN.MD was written as a parallel design doc but now needs critical updates to match the **actual implemented backend APIs, enums, and message formats**.

Key changes:
- ✅ **Phase 1-4**: No major API changes needed (endpoints exist as planned)
- ⚠️ **Phase 5 (WebSocket)**: Client message format differs from plan
- ⚠️ **Phase 6 (Auth)**: OAuth flow documentation needs clarification
- ✅ **Phase 7-11**: Components/utilities align with backend but need minor tweaks

---

## CRITICAL CHANGES FOR UI_PLAN.MD

### 1. WebSocket Client Message Format (Phase 5 - URGENT)

**UI_PLAN currently says**:
```javascript
// Line 168, UI_PLAN.MD Phase 5
client.send('/app/subscribe', {}, JSON.stringify({ type: "subscribe", interval: 60000 }));
```

**Actually implemented in backend** (`NotificationHandler.java`):
```java
// Server expects ONLY interval field
// No "type" field in subscribe message
```

**Updated format** (for UI_PLAN.MD Phase 5):
```typescript
// Phase 5: WebSocket Integration - corrected

// SUBSCRIBE (updated: NO type field)
client.send('/app/subscribe', {}, JSON.stringify({ 
  interval: 60000  // milliseconds - that's it
}));

// ACKNOWLEDGE (keep as-is)
client.send('/app/ack', {}, JSON.stringify({
  type: 'ack',
  notificationId: '550e8400-e29b-41d4-a716-446655440000'
}));

// SERVER NOTIFICATION (keep as-is)
// Server → Client message:
{
  "type": "notification",
  "id": "uuid",
  "taskId": "task-uuid",
  "title": "Task title",
  "reason": "overdue" | "due-soon",
  "dueDate": "2026-05-20",
  "priority": "LOW" | "MEDIUM" | "HIGH",
  "sentAt": "2026-05-21T10:30:00Z"
}
```

**Action**: Update `Phase 5` section and `notificationService.ts` stub in UI_PLAN to remove `type` field from subscribe.

---

### 2. Task Enums - Use Proper Values (Phase 8 + Components)

**Backend implements** (confirmed in `TaskStatus.java`, `TaskPriority.java`):
```java
TaskStatus: TO_DO, IN_PROGRESS, DONE
TaskPriority: LOW, MEDIUM, HIGH
```

**Update UI_PLAN Phase 8** (Task Components) to reference these exact enum values everywhere:
- `TaskStatusQuickChange.vue`: Radio buttons must use `TO_DO`, `IN_PROGRESS`, `DONE` (not `todo`, `inProgress`, `done`)
- `TaskFilterBar.vue`: Status filter options = `TO_DO | IN_PROGRESS | DONE`
- `Badge.vue`: Map enums to display labels ("To Do", "In Progress", "Done")

---

### 3. OAuth Login Flow - Backend-Driven (Phase 8 Auth)

**UI_PLAN currently says** (Line 288-293):
```javascript
// LoginPage.vue - "No OAuth client IDs in frontend"
// Backend handles OAuth flow
// After backend OAuth exchange, backend redirects back to app with session cookie
// Frontend detects user logged in via `GET /api/auth/me`
```

**This is CORRECT** but needs clarification in UI_PLAN:

**Updated text** (for Phase 8 - LoginPage.vue):
```markdown
### Create `src/components/auth/LoginPage.vue`:
- Render two buttons:
  - "Continue with Google" → redirects to `GET /api/oauth2/authorization/google` (backend initiates OAuth flow)
  - "Continue with GitHub" → redirects to `GET /api/oauth2/authorization/github` (backend initiates OAuth flow)
  
- **Backend OAuth Flow**:
  1. User clicks button → redirects to backend OAuth endpoint
  2. Backend redirects to Google/GitHub provider login
  3. After login, provider redirects to backend callback URL
  4. Backend exchanges OAuth code for user info
  5. Backend creates session + HttpOnly cookie
  6. Backend redirects to frontend at `http://app.localhost:5173` (or configured redirect)
  
- **Frontend After OAuth**:
  1. Frontend detects it was redirected back to home (no action needed)
  2. Frontend calls `GET /api/auth/me` to check if logged in
  3. If 200 OK + user data → user is authenticated
  4. If 401 Unauthorized → user not logged in, show login page
  
- **Note**: No OAuth client IDs or tokens in frontend. All OAuth details handled server-side.
```

---

### 4. API Client - Session Cookies Configuration (Phase 4 - Critical)

**UI_PLAN Phase 4 mentions** (Line 119):
```typescript
// CRITICAL: Set `withCredentials: true` for session cookies
```

**Correct, but add to Phase 1 Vite config notes** (Line 27):
```markdown
- [ ] Configure `vite.config.ts`:
  - [ ] API proxy for backend: `/api` → `http://api.localhost:8080/api`
  - [ ] **IMPORTANT**: Vite proxy must pass cookies: add `credentials: 'include'` to proxy config
  - [ ] Rewrite cookie domain to `.localhost` for same-domain cookies
  - [ ] WebSocket proxy: `/api/ws` → `ws://api.localhost:8080/api/ws` (also with credentials)
```

---

### 5. Task API Endpoints - Actual Paths (Phase 4 + Phase 8)

**Confirmed endpoints** (from API.md + backend implementation):

```
Nested (List-Scoped):
GET    /api/lists/{listId}/tasks                    ✅
GET    /api/lists/{listId}/tasks/{taskId}           ✅
POST   /api/lists/{listId}/tasks                     ✅
PUT    /api/lists/{listId}/tasks/{taskId}           ✅
DELETE /api/lists/{listId}/tasks/{taskId}           ✅

Global (User-Wide):
GET    /api/tasks                                   ✅
GET    /api/tasks?status=TO_DO&priority=HIGH       ✅
GET    /api/tasks/search?q=query                    ✅
GET    /api/tasks/overdue                           ✅
GET    /api/tasks/due-soon                          ✅
PATCH  /api/tasks/{taskId}/status?status=IN_PROGRESS ✅
```

**UI_PLAN Phase 4** (API Client - taskApi.ts) is **correct as-is**. No changes needed.

---

### 6. Form Validation - Add Backend Error Handling (Phase 11)

**UI_PLAN Phase 11** mentions validation but needs to add:
- List title: must be non-blank (backend returns 400 Bad Request if blank)
- Task title: must be non-blank
- Due date: optional, but if provided must be valid date (backend validates)
- Form error handling: display error toasts when API returns 400/403/404

**Add to Phase 11** (after line 450):
```markdown
- [ ] Handle API error responses:
  - 400 Bad Request → show validation errors from backend
  - 403 Forbidden → "You don't have permission to modify this resource"
  - 404 Not Found → "Resource not found or deleted"
  - 409 Conflict → "Cannot delete list with active tasks"
  - 500 Server Error → "Server error, please try again"
```

---

### 7. Notification Toast Format - Match Backend (Phase 8)

**UI_PLAN Phase 8** NotificationToast.vue:
```markdown
- [ ] Create `src/components/notifications/NotificationToast.vue`:
  - [ ] Display notification with task title, reason, due date
  - [ ] Acknowledge button
  - [ ] Auto-dismiss or manual close
  - [ ] Stack multiple toasts
```

**Update to include actual backend fields**:
```markdown
- [ ] Create `src/components/notifications/NotificationToast.vue`:
  - [ ] Display:
    - Task title
    - Reason badge ("Overdue" or "Due Soon")
    - Due date (formatted)
    - Priority badge (color-coded)
  - [ ] Acknowledge button → sends ack message to WebSocket
  - [ ] Auto-dismiss after 5 seconds (configurable)
  - [ ] Stacking: newer toasts appear at top/bottom
  - [ ] Map backend reason to human text:
    - "overdue" → "Overdue"
    - "due-soon" → "Due Soon"
```

---

### 8. Responsive Design - No Changes (Phase 14)

**UI_PLAN Phase 14** breakpoints are correct:
- 320px (mobile)
- 768px (tablet)
- 1440px (desktop)

✅ **No changes needed.**

---

### 9. Documentation - Add Backend References (Phase 15)

**UI_PLAN Phase 15** (Documentation) should reference backend docs:

**Add to Phase 15**:
```markdown
- [ ] Reference backend documentation:
  - API spec: See `/API.md` for complete endpoint reference
  - WebSocket format: See `/README.md` → WebSocket Notifications section
  - Task enums: Status (TO_DO, IN_PROGRESS, DONE), Priority (LOW, MEDIUM, HIGH)
  - Authorization: @PreAuthorize on edit/delete endpoints (returns 403 if unauthorized)
```

---

## NO CHANGES NEEDED (Already Aligned)

✅ **Phase 1 (Project Setup)**: Dependencies list is correct  
✅ **Phase 2 (Global Styles)**: Tailwind breakpoints fine  
✅ **Phase 3 (Pinia stores)**: Store structure matches backend entities  
✅ **Phase 4 (API client)**: Endpoints are correct  
✅ **Phase 6 (Composables)**: useQuery/useMutation patterns fit backend  
✅ **Phase 7 (Base components)**: Button, Input, Modal, etc. are generic  
✅ **Phase 9 (Pages/Views)**: HomePage, AuthPage structure correct  
✅ **Phase 10 (Routing)**: Route guards and auth check correct  
✅ **Phase 12-13 (Tests)**: Test file names and strategy correct  

---

## Summary of Actions

| Priority | Location | Change | Reason |
|----------|----------|--------|--------|
| 🔴 **CRITICAL** | Phase 5 | Remove `type` field from subscribe message | Backend doesn't expect it |
| 🟡 **HIGH** | Phase 8 | Clarify OAuth flow step-by-step | Current text is vague |
| 🟡 **HIGH** | Phase 4 | Add Vite proxy credential config | Session cookies need it |
| 🟡 **HIGH** | Phase 8 | Use exact enum values (TO_DO not todo) | API uses uppercase enums |
| 🟢 **MEDIUM** | Phase 8 | Update NotificationToast with backend fields | Better UX documentation |
| 🟢 **MEDIUM** | Phase 11 | Add HTTP error code handling | Currently missing 409/403/etc |
| 🟢 **LOW** | Phase 15 | Add backend documentation references | Makes onboarding easier |

---

## Ready to Proceed?

Once you confirm these changes, I can:
1. ✅ Update UI_PLAN.MD with all corrections
2. ✅ Highlight the critical WebSocket message format change
3. ✅ Add OAuth flow diagram/steps
4. ✅ Provide updated component stubs with correct enum values

**Recommendation**: Review the **CRITICAL** changes first (WebSocket format, OAuth flow), then proceed with Phase 1 setup.
