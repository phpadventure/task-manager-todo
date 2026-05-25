# Frontend Implementation Plan for Personal To-Do Manager

## Context
This project requires building a responsive Vue 3 SPA with Vite that connects to the backend APIs for task management. The frontend handles OAuth2 login redirects, displays task lists with real-time WebSocket notifications, supports search/filtering, and manages form validation. Single-user per session with no data sharing.

**Current State**: Only Vue 3 + Vite project structure; no components, stores, or pages created.

**Tech Stack**: Vue 3, Vite, Tailwind CSS, Headless UI (or shadcn), TanStack Query (Vue Query), Pinia, WebSocket API, Vitest, @vue/test-utils, Playwright

*** local backend api running at `http://localhost:8080/api/` and openapi is available at http://localhost:8080/api/api-docs ***

---

## Phase 1: Project Setup & Configuration

- [x] Initialize Vite + Vue 3 project with TypeScript
- [x] Install dependencies: `npm install`
  - [x] `@tanstack/vue-query` (data fetching)
  - [x] `pinia` (state management)
  - [x] `tailwindcss` + `@headlessui/vue` (UI)
  - [x] `axios` (HTTP client with credentials: 'include' for cookies)
  - [x] `ws` (WebSocket client)
  - [x] `vitest`, `@vue/test-utils` (testing)
  - [x] `playwright` (E2E testing)
- [x] Configure `vite.config.ts`:
  - [x] API proxy for backend: `/api` → `http://localhost:8080/api` with `credentials: 'include'` and `changeOrigin: true`
  - [x] **CRITICAL**: Add `credentials: 'include'` to proxy config for session cookies
  - [x] WebSocket proxy: `/api/ws` → `ws://localhost:8080/api/ws` with `credentials: 'include'` and `changeOrigin: true`
  - [x] Alias `@` for `src` directory
  - [x] Rewrite cookie domain to `.localhost` for same-domain cookies (optional, for Docker compatibility)
- [x] Configure `tsconfig.json` for Vue 3 + TypeScript
- [x] Setup Tailwind CSS configuration
- [x] Create `.env.example` with `VITE_API_URL` (backend base URL)
- [x] Create `.env.development` for local dev settings
  - [x] `VITE_API_URL=http://localhost:8080` (or environment-specific URL)

**Note**: No OAUTH_*_CLIENT_ID in frontend `.env`—OAuth redirects handled by backend (SecurityConfig redirects to provider).

**Files to Create**:
- [ ] `ui/vite.config.ts`
- [ ] `ui/tsconfig.json`
- [ ] `ui/tsconfig.app.json`
- [ ] `ui/tailwind.config.js`
- [ ] `ui/postcss.config.js`
- [ ] `ui/.env.example`
- [ ] `ui/.env.development`
- [ ] `ui/vitest.config.ts`
- [ ] `ui/playwright.config.ts`

---

## Phase 2: Global Styles & Layout Structure

- [x] Create `src/styles/globals.css` with Tailwind imports and global utilities
- [x] Create `src/layouts/MainLayout.vue` (sidebar + main content area)
- [x] Create `src/layouts/AuthLayout.vue` (centered auth page)
- [x] Setup responsive breakpoints (320px, 768px, 1440px)
- [x] Create reusable CSS utilities for common patterns

**Files to Create**:
- [ ] `ui/src/styles/globals.css`
- [ ] `ui/src/styles/variables.css`
- [ ] `ui/src/layouts/MainLayout.vue`
- [ ] `ui/src/layouts/AuthLayout.vue`

---

## Phase 3: State Management with Pinia

- [x] Create `src/stores/authStore.ts`:
  - [x] `user` state (id, email, displayName, avatarUrl)
  - [x] `isAuthenticated` flag
  - [x] `loading` state for login
  - [x] `currentUser()` getter
  - [x] `setUser(user)` action
  - [x] `logout()` action
  - [x] `fetchCurrentUser()` async action

- [x] Create `src/stores/listStore.ts`:
  - [x] `lists` array
  - [x] `selectedListId` state
  - [x] `loading`, `error` states
  - [x] `getListById(id)` getter
  - [x] `setLists(lists)` action
  - [x] `setSelectedList(id)` action
  - [x] `addList(list)` action
  - [x] `updateList(id, list)` action
  - [x] `deleteList(id)` action

- [x] Create `src/stores/taskStore.ts`:
  - [x] `tasks` array
  - [x] `filters` state (status, priority, dueDate, searchQuery)
  - [x] `loading`, `error` states
  - [x] `getTasksByListId(listId)` getter with filter logic
  - [x] `getTaskById(id)` getter
  - [x] `setTasks(tasks)` action
  - [x] `setFilters(filters)` action
  - [x] `addTask(task)` action
  - [x] `updateTask(id, task)` action
  - [x] `deleteTask(id)` action
  - [x] `clearFilters()` action

- [x] Create `src/stores/notificationStore.ts`:
  - [x] `notifications` array with id, taskId, title, reason, due_date
  - [x] `addNotification(notification)` action
  - [x] `removeNotification(id)` action
  - [x] `acknowledgeNotification(id)` action
  - [x] Auto-dismiss after 5 seconds

**Files to Create**:
- [ ] `ui/src/stores/authStore.ts`
- [ ] `ui/src/stores/listStore.ts`
- [ ] `ui/src/stores/taskStore.ts`
- [ ] `ui/src/stores/notificationStore.ts`

---

## Phase 4: API Client & Utilities

- [x] Create `src/api/client.ts`:
  - [x] Axios instance with base URL from env
  - [x] **CRITICAL**: Set `withCredentials: true` for session cookies
  - [x] Request/response interceptors
  - [x] Error handling and status code mapping
  - [x] Handle 401 Unauthorized → redirect to login page

- [x] Create `src/api/authApi.ts`:
  - [x] `getCurrentUser()` - GET /api/auth/me
  - [x] `logout()` - POST /logout

- [x] Create `src/api/listApi.ts`:
  - [x] `getLists()` - GET /api/lists
  - [x] `createList(title)` - POST /api/lists
  - [x] `updateList(id, title)` - PUT /api/lists/{id}
  - [x] `deleteList(id)` - DELETE /api/lists/{id}

- [x] Create `src/api/taskApi.ts`:
  - [x] `getTasks(listId, filters)` - GET /api/tasks with query params
  - [x] `searchTasks(query)` - GET /api/tasks/search?q={query}
  - [x] `createTask(data)` - POST /api/tasks
  - [x] `updateTask(id, data)` - PUT /api/tasks/{id}
  - [x] `updateTaskStatus(id, status)` - PATCH /api/tasks/{id}/status
  - [x] `deleteTask(id)` - DELETE /api/tasks/{id}

- [x] Create `src/utils/dateUtils.ts`:
  - [x] `formatDate(date)` - display format
  - [x] `isOverdue(dueDate)` - check if overdue
  - [x] `isDueSoon(dueDate)` - check if due within 3 days
  - [x] `getDueDateLabel(dueDate)` - return "Overdue", "Today", "Tomorrow", etc.

- [x] Create `src/utils/filterUtils.ts`:
  - [x] `getDefaultFilters()` - initial filter state
  - [x] `buildQueryParams(filters)` - convert filters to API query string
  - [x] `validateFilters(filters)` - check valid status/priority values

**Files to Create**:
- [ ] `ui/src/api/client.ts`
- [ ] `ui/src/api/authApi.ts`
- [ ] `ui/src/api/listApi.ts`
- [ ] `ui/src/api/taskApi.ts`
- [ ] `ui/src/utils/dateUtils.ts`
- [ ] `ui/src/utils/filterUtils.ts`

---

## Phase 5: WebSocket Integration

- [x] Create `src/websocket/notificationService.ts`:
  - [x] `connect()` - establish WebSocket connection to /api/ws
  - [x] `disconnect()` - close connection
  - [x] `subscribe(interval)` - send subscribe message with ONLY `interval` field (milliseconds)
  - [x] `acknowledge(notificationId)` - send `{ type: "ack", notificationId }`
  - [x] Event listeners: onMessage, onError, onClose
  - [x] Auto-reconnect logic with exponential backoff

- [x] Create `src/composables/useNotifications.ts`:
  - [x] `connect()` hook to init WebSocket on app mount
  - [x] `disconnect()` hook to cleanup
  - [x] Handle incoming notifications (fields: type, id, taskId, title, reason, dueDate, priority, sentAt)
  - [x] Add notifications to notificationStore
  - [x] Filter duplicates using seen notification IDs

**Message Formats** (Backend-Confirmed):
```typescript
// Client → Server: Subscribe (⚠️ NO type field - backend expects ONLY interval)
{ 
  interval: 60000  // milliseconds
}

// Client → Server: Acknowledge
{ 
  type: "ack", 
  notificationId: "uuid" 
}

// Server → Client: Notification
{
  type: "notification",
  id: "uuid",
  taskId: "task-uuid",
  title: "Buy groceries",
  reason: "overdue" | "due-soon",
  dueDate: "2026-05-20",
  priority: "LOW" | "MEDIUM" | "HIGH",
  sentAt: "2026-05-21T10:30:00Z"
}
```

**Critical Note**: Subscribe message ONLY contains `interval` field. Do NOT add `type` field.

**Files to Create**:
- [ ] `ui/src/websocket/notificationService.ts`
- [ ] `ui/src/composables/useNotifications.ts`

---

## Phase 6: Composables & Reusable Logic

- [x] Create `src/composables/useAuth.ts`:
  - [x] `user` ref from authStore
  - [x] `isAuthenticated` computed
  - [x] `login()` - redirect to OAuth
  - [x] `logout()` - clear auth
  - [x] `getCurrentUser()` - fetch from API

- [x] Create `src/composables/useLists.ts`:
  - [x] Query wrapper for `getLists()`
  - [x] `createList(title)` mutation
  - [x] `updateList(id, title)` mutation
  - [x] `deleteList(id)` mutation
  - [x] Error handling

- [x] Create `src/composables/useTasks.ts`:
  - [x] Query for `getTasks(listId, filters)`
  - [x] Query for `searchTasks(query)`
  - [x] `createTask()`, `updateTask()`, `deleteTask()` mutations
  - [x] `updateTaskStatus()` mutation
  - [x] Refetch logic after mutations

- [x] Create `src/composables/useForm.ts`:
  - [x] Form state management with validation
  - [x] `handleSubmit()` wrapper
  - [x] Error display helpers

- [x] Create `src/composables/useLocalStorage.ts`:
  - [x] Persist filter preferences
  - [x] Persist selected list ID

**Files to Create**:
- [ ] `ui/src/composables/useAuth.ts`
- [ ] `ui/src/composables/useLists.ts`
- [ ] `ui/src/composables/useTasks.ts`
- [ ] `ui/src/composables/useForm.ts`
- [ ] `ui/src/composables/useLocalStorage.ts`

---

## Phase 7: Reusable UI Components

### Base Components (Headless UI wrappers)
- [x] Create `src/components/common/Button.vue`
  - [x] Variants: primary, secondary, danger, ghost
  - [x] States: loading, disabled
  - [x] Sizes: sm, md, lg

- [x] Create `src/components/common/Input.vue`
  - [x] Text, email, date, number types
  - [x] Error state and message
  - [x] Label and placeholder

- [x] Create `src/components/common/Select.vue`
  - [x] Dropdown with options
  - [x] Multi-select variant
  - [x] Error state

- [ ] Create `src/components/common/Modal.vue`
  - [ ] Header, body, footer slots
  - [ ] Close button
  - [ ] Focus trap, backdrop

- [ ] Create `src/components/common/Drawer.vue`
  - [ ] Slide-in panel (mobile-friendly)
  - [ ] Close button
  - [ ] Overlay

- [ ] Create `src/components/common/Spinner.vue`
  - [ ] Loading indicator
  - [ ] Sizes: sm, md, lg

- [ ] Create `src/components/common/EmptyState.vue`
  - [ ] Icon, heading, description
  - [ ] Optional action button

- [ ] Create `src/components/common/Toast.vue`
  - [ ] For notifications
  - [ ] Auto-dismiss
  - [ ] Types: success, error, info, warning

- [x] Create `src/components/common/Badge.vue`
  - [x] Status badge mapping (use exact enum values):
    - `TO_DO` → display "To Do" (gray)
    - `IN_PROGRESS` → display "In Progress" (yellow)
    - `DONE` → display "Done" (green)
  - [x] Priority badge mapping (use exact enum values):
    - `LOW` → display "Low" (blue)
    - `MEDIUM` → display "Medium" (orange)
    - `HIGH` → display "High" (red)
  - [x] Color coding consistent across app

**Files to Create**:
- [ ] `ui/src/components/common/Button.vue`
- [ ] `ui/src/components/common/Input.vue`
- [ ] `ui/src/components/common/Select.vue`
- [ ] `ui/src/components/common/Modal.vue`
- [ ] `ui/src/components/common/Drawer.vue`
- [ ] `ui/src/components/common/Spinner.vue`
- [ ] `ui/src/components/common/EmptyState.vue`
- [ ] `ui/src/components/common/Toast.vue`
- [ ] `ui/src/components/common/Badge.vue`

---

## Phase 8: Feature Components

### Authentication Components
- [x] Create `src/components/auth/LoginPage.vue`:
  - [x] Render two buttons:
    - "Continue with Google" → redirects to `/api/oauth2/authorization/google` (backend initiates OAuth flow)
    - "Continue with GitHub" → redirects to `/api/oauth2/authorization/github` (backend initiates OAuth flow)
  - [x] Centered layout with branding
  
  - [x] **OAuth Flow (Backend-Driven - Step-by-Step)**:
    1. User clicks button → browser navigates to backend OAuth endpoint
    2. Backend redirects to Google/GitHub provider login page
    3. User logs in with provider credentials
    4. Provider redirects to backend callback URL with authorization code
    5. Backend exchanges OAuth code for user info (using provider API)
    6. Backend creates session + HttpOnly, Secure, SameSite=Lax session cookie
    7. Backend redirects to success page (e.g., `/success` or `/api/oauth-success`)
    
  - [x] **Frontend After OAuth Redirect** (Vite proxy handles redirect):
    1. Vite proxy receives redirect to success page
    2. Frontend detects user was redirected (can check via URL or session check)
    3. Frontend calls `GET /api/auth/me` to verify logged in
    4. If 200 OK + user data → user is authenticated, navigate to home
    5. If 401 Unauthorized → user not logged in, show login page
  
  - [x] **No OAuth client IDs or tokens in frontend** (all OAuth details and provider credentials handled server-side)
  - [x] Session persistence: Vite proxy + backend CORS allow session cookie with `/api` requests

- [ ] Create `src/components/auth/UserMenu.vue`:
  - [ ] Display user avatar + name
  - [ ] Dropdown with profile, settings, logout
  - [ ] Responsive mobile menu

### List/Project Components
- [x] Create `src/components/lists/ListSidebar.vue`:
  - [x] Display all user's lists
  - [x] Active list highlight
  - [x] Add new list button
  - [x] Collapsible on mobile
  - [ ] Edit/delete list actions

- [ ] Create `src/components/lists/CreateListModal.vue`:
  - [ ] Form with title input
  - [ ] Validation feedback
  - [ ] Submit/cancel buttons

- [ ] Create `src/components/lists/EditListModal.vue`:
  - [ ] Pre-populated title
  - [ ] Delete confirmation
  - [ ] Submit/cancel buttons

### Task Components
- [x] Create `src/components/tasks/TaskList.vue`:
  - [x] Display tasks as table or cards
  - [x] Render title, description, status, priority, due_date
  - [ ] Quick status change buttons
  - [ ] Edit/delete actions
  - [x] Empty state

- [ ] Create `src/components/tasks/TaskItem.vue`:
  - [ ] Individual task display
  - [ ] Status badge
  - [ ] Priority badge
  - [ ] Due date indicator
  - [ ] Hover actions

- [ ] Create `src/components/tasks/CreateTaskForm.vue`:
  - [ ] Inputs: title (required), description, dueDate, priority
  - [ ] Form validation with feedback
  - [ ] Submit/cancel buttons
  - [ ] Success message on submit

- [ ] Create `src/components/tasks/EditTaskForm.vue`:
  - [ ] Pre-populated fields
  - [ ] Validation feedback
  - [ ] Delete confirmation
  - [ ] Submit/cancel buttons

- [x] Create `src/components/tasks/TaskFilterBar.vue`:
  - [x] Search input (debounced)
  - [x] Status filter (multi-select):
    - Use enum values: `TO_DO`, `IN_PROGRESS`, `DONE` (uppercase - matches backend exactly)
    - Display labels: "To Do", "In Progress", "Done"
  - [x] Priority filter (multi-select):
    - Use enum values: `LOW`, `MEDIUM`, `HIGH` (uppercase - matches backend exactly)
    - Display labels: "Low", "Medium", "High"
  - [x] Due date filter (dropdown):
    - Options: `overdue`, `today`, `next_7_days`, `all` (lowercase backend values)
    - Display labels: "Overdue", "Today", "Next 7 Days", "All"
  - [x] Clear filters button

- [ ] Create `src/components/tasks/TaskStatusQuickChange.vue`:
  - [ ] Dropdown to change status inline
  - [ ] Use exact enum values: `TO_DO`, `IN_PROGRESS`, `DONE` (uppercase - must match backend enums)
  - [ ] Display labels: "To Do", "In Progress", "Done"
  - [ ] Send PATCH request to `/api/tasks/{taskId}/status?status={newStatus}` with uppercase enum value

### Notification Components
- [x] Create `src/components/notifications/NotificationToast.vue`:
  - [x] Display notification card with:
    - Task title (from backend notification)
    - Reason badge mapping: `"overdue"` → "Overdue", `"due-soon"` → "Due Soon"
    - Due date (formatted human-readable, e.g., "May 20, 2026")
    - Priority badge (color-coded: Low=blue, Medium=orange, High=red)
  - [x] Acknowledge button → sends `{ type: "ack", notificationId }` to WebSocket
  - [x] Manual close button (alternative to auto-dismiss)
  - [x] Auto-dismiss after 5 seconds (user can override by hovering)
  - [x] Stack multiple toasts (newer toasts appear at top/bottom - configurable)
  - [x] Fade-in/fade-out animations for UX

**Files to Create**:
- [ ] `ui/src/components/auth/LoginPage.vue`
- [ ] `ui/src/components/auth/UserMenu.vue`
- [ ] `ui/src/components/lists/ListSidebar.vue`
- [ ] `ui/src/components/lists/CreateListModal.vue`
- [ ] `ui/src/components/lists/EditListModal.vue`
- [ ] `ui/src/components/tasks/TaskList.vue`
- [ ] `ui/src/components/tasks/TaskItem.vue`
- [ ] `ui/src/components/tasks/CreateTaskForm.vue`
- [ ] `ui/src/components/tasks/EditTaskForm.vue`
- [ ] `ui/src/components/tasks/TaskFilterBar.vue`
- [ ] `ui/src/components/tasks/TaskStatusQuickChange.vue`
- [ ] `ui/src/components/notifications/NotificationToast.vue`

---

## Phase 9: Pages/Views

- [x] Create `src/pages/AuthPage.vue`:
  - [x] Render `LoginPage` component
  - [x] Redirect to home if already authenticated
  - [x] Handle OAuth callback

- [x] Create `src/pages/HomePage.vue`:
  - [x] Layout: sidebar + main content
  - [x] List sidebar showing all lists
  - [x] Main area with task list or empty state
  - [x] Render task list + filter bar
  - [x] Create/edit task modals
  - [x] Load initial data on mount

- [ ] Create `src/pages/TaskDetailPage.vue` (optional):
  - [ ] Full task detail view
  - [ ] Edit form
  - [ ] Delete action
  - [ ] Comments section (future feature)

**Files to Create**:
- [ ] `ui/src/pages/AuthPage.vue`
- [ ] `ui/src/pages/HomePage.vue`
- [ ] `ui/src/pages/TaskDetailPage.vue`

---

## Phase 10: Routing & Main App

- [x] Create `src/router/index.ts`:
  - [x] Define routes: /, /auth, /home
  - [x] Meta guards for authentication
  - [x] Redirect unauthenticated users to /auth
  - [x] Redirect authenticated users from /auth to /home

- [x] Create `src/App.vue`:
  - [x] Root component with router-view
  - [x] Global notification toast container
  - [x] Loading overlay for app-level loading

- [x] Create `src/main.ts`:
  - [x] Initialize Vue app
  - [x] Setup Pinia
  - [x] Setup Vue Query (TanStack Query)
  - [x] Setup router
  - [x] Setup WebSocket notifications
  - [x] Mount to DOM

**Files to Create**:
- [ ] `ui/src/router/index.ts`
- [ ] `ui/src/App.vue`
- [ ] `ui/src/main.ts`
- [ ] `ui/src/index.html`

---

## Phase 11: Form Validation & Error Handling

- [ ] Create `src/utils/validation.ts`:
  - [ ] `validateTaskTitle(title)` - required, max 255 chars
  - [ ] `validateTaskDescription(desc)` - optional, max 5000 chars
  - [ ] `validateListTitle(title)` - required, max 255 chars
  - [ ] `validateDueDate(date)` - optional, must be today or future if provided
  - [ ] `getErrorMessage(field, error)` - user-friendly error messages

- [ ] Create error handling in API calls (add to `src/api/client.ts`):
  - [ ] Display error toasts on API failures (use Toast component)
  - [ ] Handle HTTP status codes:
    - `400 Bad Request` → show validation error from backend response body
    - `401 Unauthorized` → clear session, redirect to login page, show "Session expired"
    - `403 Forbidden` → show "You don't have permission to modify this resource"
    - `404 Not Found` → show "Resource not found or has been deleted"
    - `409 Conflict` → show conflict-specific message (e.g., "Cannot delete list with active tasks")
    - `500 Server Error` → show "Server error, please try again"
  - [ ] Auto-retry logic for transient failures (5xx errors, optional exponential backoff)
  - [ ] Log error details to console for debugging (development mode)

**Files to Create**:
- [ ] `ui/src/utils/validation.ts`

---

## Phase 12: Component Tests with Vitest

- [ ] Create `src/components/common/__tests__/Button.spec.ts`
- [ ] Create `src/components/common/__tests__/Input.spec.ts`
- [ ] Create `src/components/common/__tests__/Modal.spec.ts`
- [ ] Create `src/components/tasks/__tests__/TaskList.spec.ts`
- [ ] Create `src/components/tasks/__tests__/TaskFilterBar.spec.ts`
- [ ] Create `src/components/tasks/__tests__/CreateTaskForm.spec.ts`
- [ ] Create `src/stores/__tests__/authStore.spec.ts`
- [ ] Create `src/stores/__tests__/taskStore.spec.ts`
- [ ] Create `src/utils/__tests__/dateUtils.spec.ts`
- [ ] Create `src/utils/__tests__/validation.spec.ts`
- [ ] Run tests: `npm run test`
- [ ] Setup code coverage: `npm run test:coverage`

**Test Coverage Goals**:
- [ ] 70%+ overall coverage
- [ ] 100% coverage for utils and stores
- [ ] All critical component interactions tested

**Files to Create**:
- [ ] `ui/src/components/common/__tests__/Button.spec.ts`
- [ ] `ui/src/components/common/__tests__/Input.spec.ts`
- [ ] `ui/src/components/common/__tests__/Modal.spec.ts`
- [ ] `ui/src/components/tasks/__tests__/TaskList.spec.ts`
- [ ] `ui/src/components/tasks/__tests__/TaskFilterBar.spec.ts`
- [ ] `ui/src/components/tasks/__tests__/CreateTaskForm.spec.ts`
- [ ] `ui/src/stores/__tests__/authStore.spec.ts`
- [ ] `ui/src/stores/__tests__/taskStore.spec.ts`
- [ ] `ui/src/utils/__tests__/dateUtils.spec.ts`
- [ ] `ui/src/utils/__tests__/validation.spec.ts`

---

## Phase 13: E2E Tests with Playwright

- [ ] Create `tests/auth.spec.ts`:
  - [ ] Login with Google OAuth
  - [ ] Login with GitHub OAuth
  - [ ] User profile displayed after login
  - [ ] Logout redirects to login page

- [ ] Create `tests/lists.spec.ts`:
  - [ ] Create new list
  - [ ] List appears in sidebar
  - [ ] Update list title
  - [ ] Delete list with confirmation

- [ ] Create `tests/tasks.spec.ts`:
  - [ ] Create task in list
  - [ ] Task appears in task list
  - [ ] Search tasks by title
  - [ ] Filter by status, priority, due date
  - [ ] Update task
  - [ ] Delete task with confirmation

- [ ] Create `tests/websocket.spec.ts`:
  - [ ] WebSocket connects on page load
  - [ ] Receive overdue task notification
  - [ ] Receive due-soon notification
  - [ ] Acknowledge notification (toast dismisses)

- [ ] Create `tests/responsive.spec.ts`:
  - [ ] Layout works at 320px (mobile)
  - [ ] Layout works at 768px (tablet)
  - [ ] Layout works at 1440px (desktop)
  - [ ] Sidebar collapses on mobile

- [ ] Create `tests/crossuser.spec.ts`:
  - [ ] User A logs in, creates tasks
  - [ ] User A logs out
  - [ ] User B logs in, cannot see User A's data
  - [ ] User B creates own tasks, isolated from User A

- [ ] Run E2E tests: `npm run test:e2e`
- [ ] View Playwright report: `npm run test:e2e -- --reporter=html`

**Files to Create**:
- [ ] `ui/tests/auth.spec.ts`
- [ ] `ui/tests/lists.spec.ts`
- [ ] `ui/tests/tasks.spec.ts`
- [ ] `ui/tests/websocket.spec.ts`
- [ ] `ui/tests/responsive.spec.ts`
- [ ] `ui/tests/crossuser.spec.ts`

---

## Phase 14: Styling & Responsive Design

- [ ] Tailwind utilities configuration
- [ ] Responsive breakpoints:
  - [ ] Mobile-first (320px base)
  - [ ] Tablet (768px)
  - [ ] Desktop (1440px)
- [ ] Dark mode support (optional)
- [ ] Accessibility (WCAG 2.1 AA):
  - [ ] Proper heading hierarchy
  - [ ] Alt text for images
  - [ ] ARIA labels for interactive elements
  - [ ] Keyboard navigation
  - [ ] Focus indicators
  - [ ] Color contrast ratios

**Tasks**:
- [ ] Test responsive at breakpoints
- [ ] Run accessibility audit (axe DevTools)
- [ ] Verify keyboard navigation
- [ ] Test with screen reader (NVDA/JAWS on Windows, VoiceOver on Mac)

---

## Phase 15: Documentation & Deployment

- [ ] Update `README.md`:
  - [ ] Frontend setup: `npm install`, `npm run dev`
  - [ ] Environment variables `.env` setup (Vite proxy configuration)
  - [ ] OAuth callback URL configuration (backend redirects to success page)
  - [ ] Component architecture overview
  - [ ] Store structure documentation (Pinia + TanStack Query)
  - [ ] API integration patterns
  - [ ] **Backend Documentation References**:
    - [ ] API Endpoints: See `/API.md` for complete endpoint reference
    - [ ] OAuth Setup: See main `README.md` → OAuth2 Authentication Setup section
    - [ ] WebSocket Format: See main `README.md` → WebSocket Notifications section
    - [ ] Task Enums: Status (`TO_DO`, `IN_PROGRESS`, `DONE`), Priority (`LOW`, `MEDIUM`, `HIGH`) - uppercase
    - [ ] Authorization: `@PreAuthorize` on edit/delete endpoints (returns 403 if unauthorized)
    - [ ] Error Handling: See main `README.md` → Error responses section
  - [ ] WebSocket message formats (subscribe message has NO type field, only interval)
  - [ ] Notification rules (overdue, due-soon)
  - [ ] Testing instructions (unit, component, E2E)
  - [ ] Vite proxy configuration for local development
  - [ ] Session-based auth with HttpOnly cookies

- [ ] Create `COMPONENT_GUIDE.md`:
  - [ ] Component props, slots, emits
  - [ ] Usage examples
  - [ ] Common patterns
  - [ ] Enum mappings (TO_DO → "To Do", etc.)

- [ ] Create `.env.example`:
  - [ ] `VITE_API_URL=http://localhost:8080` (or environment-specific)

- [ ] Setup build and deployment:
  - [ ] Build production: `npm run build`
  - [ ] Optimize bundle size
  - [ ] Setup CI/CD for frontend (GitHub Actions)
  - [ ] Docker setup (see `docker-compose.yml` for full-stack deployment)

**Files to Create/Update**:
- [ ] Update `README.md`
- [ ] Create `COMPONENT_GUIDE.md`
- [ ] Create `.env.example`

---

## Session-Based Authentication Setup (Frontend)

### Overview
Frontend uses **session-based cookies** for authentication (not JWT). Browser automatically sends HttpOnly cookies with each request.

**Key Configuration**:
1. Axios must send cookies: `withCredentials: true`
2. Vite dev server proxies `/api` to backend (preserves cookies)
3. OAuth login handled by backend (user redirected to provider, then back to app)

### Local Development Setup (Hosts File + Vite Proxy)

**Prerequisites**:
1. Backend runs on `http://localhost:8080` (via Spring Boot on port 8080)
2. Frontend runs on `http://localhost:5173` (via Vite dev server on port 5173)

### Vite Proxy Configuration

**File**: `vite.config.ts`

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        credentials: 'include'  // ⚠️ CRITICAL: Include session cookies with requests
      },
      '/api/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
        credentials: 'include'  // ⚠️ CRITICAL: Include session cookies in WebSocket upgrade
      }
    }
  }
});
```

**Important**: Without `credentials: 'include'`, session cookies won't be sent with requests and won't persist across page refresh.

**Result**:
- Frontend on `http://localhost:5173`
- Backend on `http://localhost:8080`
- Both on same `localhost` domain, cookies work automatically ✅
- API requests automatically include session cookie ✅

### Axios Client Configuration

**File**: `src/api/client.ts`

```typescript
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: '/api',  // Relative path, Vite proxy handles routing
  withCredentials: true,  // ⚠️ CRITICAL: send session cookies with ALL requests (GET, POST, etc.)
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add interceptor to handle 401 Unauthorized (session expired)
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Session expired, redirect to login
      window.location.href = '/auth';
    }
    return Promise.reject(error);
  }
);
```

**Important**: `withCredentials: true` must be set for session cookies to work. Without it, cookies won't be sent with requests.

### Startup Commands

```bash
# Terminal 1: Backend (must start first)
cd backend && ./gradlew bootRun
# Runs on http://localhost:8080

# Terminal 2: Frontend
cd ui && npm install && npm run dev
# Runs on http://localhost:5173
# Vite proxy handles /api routing to backend
```

**Test Session Persistence**:
1. Open `http://localhost:5173`
2. Click "Login with Google/GitHub" → completes OAuth flow
3. User profile displayed (via `GET /api/auth/me`)
4. Refresh page (Ctrl+R)
5. User still authenticated ✅ (session cookie persisted)
6. DevTools → Application → Cookies → session cookie present on `.localhost`

### Docker-Compose Setup (Production)

In Docker, frontend and backend served through **Nginx reverse proxy** (single origin):
- Access: `http://localhost`
- No hosts file needed
- No Vite proxy needed (backend static files served by Nginx)
- Session cookie domain: `localhost`

**Configuration**: `.claude/rules/reverse-proxy-setup.md` (Phase 6 implementation)

### Troubleshooting Session Issues

| Problem | Cause | Solution |
|---------|-------|----------|
| After login, page refresh shows 401 | Session cookie not sent | Check `withCredentials: true` in axios |
| Session cookie not in DevTools | Vite proxy not forwarding cookies | Verify `credentials: 'include'` in vite.config.ts proxy config |
| API call returns 401 after login | Cookie not being sent | Check `credentials: 'include'` in both Vite proxy AND axios withCredentials |
| CORS errors | Proxy not configured | Verify `vite.config.ts` has `/api` and `/api/ws` proxy to `http://localhost:8080` |

---

## Local Development Verification

- [ ] Install dependencies: `npm install`
- [ ] Create `.env.development` with `VITE_API_URL=http://localhost:8080`
- [ ] Start backend: `cd backend && ./gradlew bootRun` (runs on `http://localhost:8080`)
- [ ] Start dev server: `npm run dev` (runs on `http://localhost:5173`)
- [ ] Browser opens to http://localhost:5173
- [ ] Vite hot reload works
- [ ] API calls work with session cookie (check Network tab in DevTools, see JSESSIONID cookie)

### Manual Testing Checklist
- [ ] Login with Google OAuth (or mock)
- [ ] Login with GitHub OAuth (or mock)
- [ ] User profile displays correctly
- [ ] Sidebar shows user's lists
- [ ] Create new list
- [ ] Select list → shows tasks or empty state
- [ ] Create task in selected list
- [ ] Search tasks by title and description
- [ ] Filter tasks by status, priority, due date
- [ ] Quick status change works
- [ ] Edit task form pre-populates data
- [ ] Update task saves changes
- [ ] Delete task with confirmation
- [ ] WebSocket receives notifications
- [ ] Notifications toast displays and auto-dismisses
- [ ] Responsive layout at 320px, 768px, 1440px
- [ ] Logout clears session and redirects to login

### Automated Testing Checklist
- [ ] `npm run test` - all unit/component tests pass
- [ ] `npm run test:coverage` - 70%+ coverage
- [ ] `npm run test:e2e` - all E2E tests pass
- [ ] No console errors or warnings

---

## Summary

**Total files to create**: ~60 files

**Directory Structure**:
```
ui/
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── postcss.config.js
├── vitest.config.ts
├── playwright.config.ts
├── .env.example
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── index.html
│   ├── styles/
│   │   ├── globals.css
│   │   └── variables.css
│   ├── layouts/
│   │   ├── MainLayout.vue
│   │   └── AuthLayout.vue
│   ├── pages/
│   │   ├── AuthPage.vue
│   │   ├── HomePage.vue
│   │   └── TaskDetailPage.vue
│   ├── router/
│   │   └── index.ts
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   ├── Select.vue
│   │   │   ├── Modal.vue
│   │   │   ├── Drawer.vue
│   │   │   ├── Spinner.vue
│   │   │   ├── EmptyState.vue
│   │   │   ├── Toast.vue
│   │   │   └── Badge.vue
│   │   ├── auth/
│   │   │   ├── LoginPage.vue
│   │   │   └── UserMenu.vue
│   │   ├── lists/
│   │   │   ├── ListSidebar.vue
│   │   │   ├── CreateListModal.vue
│   │   │   └── EditListModal.vue
│   │   ├── tasks/
│   │   │   ├── TaskList.vue
│   │   │   ├── TaskItem.vue
│   │   │   ├── CreateTaskForm.vue
│   │   │   ├── EditTaskForm.vue
│   │   │   ├── TaskFilterBar.vue
│   │   │   └── TaskStatusQuickChange.vue
│   │   └── notifications/
│   │       └── NotificationToast.vue
│   ├── stores/
│   │   ├── authStore.ts
│   │   ├── listStore.ts
│   │   ├── taskStore.ts
│   │   └── notificationStore.ts
│   ├── api/
│   │   ├── client.ts
│   │   ├── authApi.ts
│   │   ├── listApi.ts
│   │   └── taskApi.ts
│   ├── websocket/
│   │   └── notificationService.ts
│   ├── composables/
│   │   ├── useAuth.ts
│   │   ├── useLists.ts
│   │   ├── useTasks.ts
│   │   ├── useForm.ts
│   │   ├── useLocalStorage.ts
│   │   └── useNotifications.ts
│   └── utils/
│       ├── dateUtils.ts
│       ├── filterUtils.ts
│       ├── validation.ts
│       └── __tests__/
├── tests/
│   ├── auth.spec.ts
│   ├── lists.spec.ts
│   ├── tasks.spec.ts
│   ├── websocket.spec.ts
│   ├── responsive.spec.ts
│   └── crossuser.spec.ts
└── src/components/common/__tests__/
    ├── Button.spec.ts
    ├── Input.spec.ts
    └── Modal.spec.ts
```

---

## Key Implementation Considerations

### State Management (Pinia)
- Keep stores lean with only essential state
- Use getters for derived state
- Offload API calls to composables using TanStack Query
- Clear data on logout

### Data Fetching (TanStack Query)
- Wrap all API calls in useQuery/useMutation
- Auto-retry on failure
- Cache responses with sensible TTLs
- Refetch on focus (optional)
- Handle loading and error states

### Form Validation
- Validate on blur for better UX
- Show field-level error messages
- Disable submit until form is valid
- Clear errors on input change

### Accessibility
- Use semantic HTML (button, input, form, etc.)
- ARIA labels for hidden content
- Keyboard navigation (Tab, Enter, Escape)
- Focus management in modals
- Color contrast 4.5:1 for text

### Performance
- Code split pages with dynamic imports
- Lazy load components with defineAsyncComponent
- Optimize images (webp, srcset)
- Minify CSS/JS in production
- Monitor bundle size with `npm run build -- --analyze`

### Error Handling
- User-friendly error messages
- Retry mechanism for transient failures
- Fallback UI for missing data
- Global error boundary (optional)

---

## Next Steps After Plan Approval
1. Initialize Vite project with Vue 3 + TypeScript
2. Setup Tailwind CSS, Pinia, Vue Query
3. Create API client and utilities
4. Build state management stores
5. Create base UI components (Button, Input, Modal, etc.)
6. Build feature components (List, Task, Auth)
7. Create pages and setup routing
8. Implement WebSocket notifications
9. Add form validation and error handling
10. Write comprehensive unit and E2E tests
11. Optimize performance and accessibility
12. Update documentation
