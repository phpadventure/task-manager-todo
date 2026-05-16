# Frontend Implementation Plan for Personal To-Do Manager

## Context
This project requires building a responsive Vue 3 SPA with Vite that connects to the backend APIs for task management. The frontend handles OAuth2 login redirects, displays task lists with real-time WebSocket notifications, supports search/filtering, and manages form validation. Single-user per session with no data sharing.

**Current State**: Only Vue 3 + Vite project structure; no components, stores, or pages created.

**Tech Stack**: Vue 3, Vite, Tailwind CSS, Headless UI (or shadcn), TanStack Query (Vue Query), Pinia, WebSocket API, Vitest, @vue/test-utils, Playwright

---

## Phase 1: Project Setup & Configuration

- [ ] Initialize Vite + Vue 3 project with TypeScript
- [ ] Install dependencies: `npm install`
  - [ ] `@tanstack/vue-query` (data fetching)
  - [ ] `pinia` (state management)
  - [ ] `tailwindcss` + `@headlessui/vue` (UI)
  - [ ] `axios` (HTTP client)
  - [ ] `ws` (WebSocket client)
  - [ ] `vitest`, `@vue/test-utils` (testing)
  - [ ] `playwright` (E2E testing)
- [ ] Configure `vite.config.ts`:
  - [ ] API proxy for backend: `/api` → `http://localhost:8080/api`
  - [ ] WebSocket proxy: `/api/ws` → `ws://localhost:8080/api/ws`
  - [ ] Alias `@` for `src` directory
- [ ] Configure `tsconfig.json` for Vue 3 + TypeScript
- [ ] Setup Tailwind CSS configuration
- [ ] Create `.env.example` with `VITE_API_URL`, `VITE_OAUTH_GOOGLE_CLIENT_ID`, `VITE_OAUTH_GITHUB_CLIENT_ID`
- [ ] Create `.env.development` for local dev settings

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

- [ ] Create `src/styles/globals.css` with Tailwind imports and global utilities
- [ ] Create `src/layouts/MainLayout.vue` (sidebar + main content area)
- [ ] Create `src/layouts/AuthLayout.vue` (centered auth page)
- [ ] Setup responsive breakpoints (320px, 768px, 1440px)
- [ ] Create reusable CSS utilities for common patterns

**Files to Create**:
- [ ] `ui/src/styles/globals.css`
- [ ] `ui/src/styles/variables.css`
- [ ] `ui/src/layouts/MainLayout.vue`
- [ ] `ui/src/layouts/AuthLayout.vue`

---

## Phase 3: State Management with Pinia

- [ ] Create `src/stores/authStore.ts`:
  - [ ] `user` state (id, email, displayName, avatarUrl)
  - [ ] `isAuthenticated` flag
  - [ ] `loading` state for login
  - [ ] `currentUser()` getter
  - [ ] `setUser(user)` action
  - [ ] `logout()` action
  - [ ] `fetchCurrentUser()` async action

- [ ] Create `src/stores/listStore.ts`:
  - [ ] `lists` array
  - [ ] `selectedListId` state
  - [ ] `loading`, `error` states
  - [ ] `getListById(id)` getter
  - [ ] `setLists(lists)` action
  - [ ] `setSelectedList(id)` action
  - [ ] `addList(list)` action
  - [ ] `updateList(id, list)` action
  - [ ] `deleteList(id)` action

- [ ] Create `src/stores/taskStore.ts`:
  - [ ] `tasks` array
  - [ ] `filters` state (status, priority, dueDate, searchQuery)
  - [ ] `loading`, `error` states
  - [ ] `getTasksByListId(listId)` getter with filter logic
  - [ ] `getTaskById(id)` getter
  - [ ] `setTasks(tasks)` action
  - [ ] `setFilters(filters)` action
  - [ ] `addTask(task)` action
  - [ ] `updateTask(id, task)` action
  - [ ] `deleteTask(id)` action
  - [ ] `clearFilters()` action

- [ ] Create `src/stores/notificationStore.ts`:
  - [ ] `notifications` array with id, taskId, title, reason, due_date
  - [ ] `addNotification(notification)` action
  - [ ] `removeNotification(id)` action
  - [ ] `acknowledgeNotification(id)` action
  - [ ] Auto-dismiss after 5 seconds

**Files to Create**:
- [ ] `ui/src/stores/authStore.ts`
- [ ] `ui/src/stores/listStore.ts`
- [ ] `ui/src/stores/taskStore.ts`
- [ ] `ui/src/stores/notificationStore.ts`

---

## Phase 4: API Client & Utilities

- [ ] Create `src/api/client.ts`:
  - [ ] Axios instance with base URL from env
  - [ ] Request/response interceptors
  - [ ] Error handling and status code mapping

- [ ] Create `src/api/authApi.ts`:
  - [ ] `getCurrentUser()` - GET /api/auth/me
  - [ ] `logout()` - POST /logout

- [ ] Create `src/api/listApi.ts`:
  - [ ] `getLists()` - GET /api/lists
  - [ ] `createList(title)` - POST /api/lists
  - [ ] `updateList(id, title)` - PUT /api/lists/{id}
  - [ ] `deleteList(id)` - DELETE /api/lists/{id}

- [ ] Create `src/api/taskApi.ts`:
  - [ ] `getTasks(listId, filters)` - GET /api/tasks with query params
  - [ ] `searchTasks(query)` - GET /api/tasks/search?q={query}
  - [ ] `createTask(data)` - POST /api/tasks
  - [ ] `updateTask(id, data)` - PUT /api/tasks/{id}
  - [ ] `updateTaskStatus(id, status)` - PATCH /api/tasks/{id}/status
  - [ ] `deleteTask(id)` - DELETE /api/tasks/{id}

- [ ] Create `src/utils/dateUtils.ts`:
  - [ ] `formatDate(date)` - display format
  - [ ] `isOverdue(dueDate)` - check if overdue
  - [ ] `isDueSoon(dueDate)` - check if due within 3 days
  - [ ] `getDueDateLabel(dueDate)` - return "Overdue", "Today", "Tomorrow", etc.

- [ ] Create `src/utils/filterUtils.ts`:
  - [ ] `getDefaultFilters()` - initial filter state
  - [ ] `buildQueryParams(filters)` - convert filters to API query string
  - [ ] `validateFilters(filters)` - check valid status/priority values

**Files to Create**:
- [ ] `ui/src/api/client.ts`
- [ ] `ui/src/api/authApi.ts`
- [ ] `ui/src/api/listApi.ts`
- [ ] `ui/src/api/taskApi.ts`
- [ ] `ui/src/utils/dateUtils.ts`
- [ ] `ui/src/utils/filterUtils.ts`

---

## Phase 5: WebSocket Integration

- [ ] Create `src/websocket/notificationService.ts`:
  - [ ] `connect()` - establish WebSocket connection to /api/ws
  - [ ] `disconnect()` - close connection
  - [ ] `subscribe(interval)` - send `{ type: "subscribe", interval }`
  - [ ] `acknowledge(notificationId)` - send `{ type: "ack", notificationId }`
  - [ ] Event listeners: onMessage, onError, onClose
  - [ ] Auto-reconnect logic with exponential backoff

- [ ] Create `src/composables/useNotifications.ts`:
  - [ ] `connect()` hook to init WebSocket on app mount
  - [ ] `disconnect()` hook to cleanup
  - [ ] Handle incoming notifications → add to notificationStore
  - [ ] Filter duplicates using seen notification IDs

**Files to Create**:
- [ ] `ui/src/websocket/notificationService.ts`
- [ ] `ui/src/composables/useNotifications.ts`

---

## Phase 6: Composables & Reusable Logic

- [ ] Create `src/composables/useAuth.ts`:
  - [ ] `user` ref from authStore
  - [ ] `isAuthenticated` computed
  - [ ] `login()` - redirect to OAuth
  - [ ] `logout()` - clear auth
  - [ ] `getCurrentUser()` - fetch from API

- [ ] Create `src/composables/useLists.ts`:
  - [ ] Query wrapper for `getLists()`
  - [ ] `createList(title)` mutation
  - [ ] `updateList(id, title)` mutation
  - [ ] `deleteList(id)` mutation
  - [ ] Error handling

- [ ] Create `src/composables/useTasks.ts`:
  - [ ] Query for `getTasks(listId, filters)`
  - [ ] Query for `searchTasks(query)`
  - [ ] `createTask()`, `updateTask()`, `deleteTask()` mutations
  - [ ] `updateTaskStatus()` mutation
  - [ ] Refetch logic after mutations

- [ ] Create `src/composables/useForm.ts`:
  - [ ] Form state management with validation
  - [ ] `handleSubmit()` wrapper
  - [ ] Error display helpers

- [ ] Create `src/composables/useLocalStorage.ts`:
  - [ ] Persist filter preferences
  - [ ] Persist selected list ID

**Files to Create**:
- [ ] `ui/src/composables/useAuth.ts`
- [ ] `ui/src/composables/useLists.ts`
- [ ] `ui/src/composables/useTasks.ts`
- [ ] `ui/src/composables/useForm.ts`
- [ ] `ui/src/composables/useLocalStorage.ts`

---

## Phase 7: Reusable UI Components

### Base Components (Headless UI wrappers)
- [ ] Create `src/components/common/Button.vue`
  - [ ] Variants: primary, secondary, danger, ghost
  - [ ] States: loading, disabled
  - [ ] Sizes: sm, md, lg

- [ ] Create `src/components/common/Input.vue`
  - [ ] Text, email, date, number types
  - [ ] Error state and message
  - [ ] Label and placeholder

- [ ] Create `src/components/common/Select.vue`
  - [ ] Dropdown with options
  - [ ] Multi-select variant
  - [ ] Error state

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

- [ ] Create `src/components/common/Badge.vue`
  - [ ] Status badges (todo, in-progress, done)
  - [ ] Priority badges (low, medium, high)

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
- [ ] Create `src/components/auth/LoginPage.vue`:
  - [ ] "Continue with Google" button
  - [ ] "Continue with GitHub" button
  - [ ] Centered layout with branding
  - [ ] OAuth redirect logic

- [ ] Create `src/components/auth/UserMenu.vue`:
  - [ ] Display user avatar + name
  - [ ] Dropdown with profile, settings, logout
  - [ ] Responsive mobile menu

### List/Project Components
- [ ] Create `src/components/lists/ListSidebar.vue`:
  - [ ] Display all user's lists
  - [ ] Active list highlight
  - [ ] Add new list button
  - [ ] Collapsible on mobile
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
- [ ] Create `src/components/tasks/TaskList.vue`:
  - [ ] Display tasks as table or cards
  - [ ] Render title, description, status, priority, due_date
  - [ ] Quick status change buttons
  - [ ] Edit/delete actions
  - [ ] Empty state

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

- [ ] Create `src/components/tasks/TaskFilterBar.vue`:
  - [ ] Search input (debounced)
  - [ ] Status filter (multi-select)
  - [ ] Priority filter (multi-select)
  - [ ] Due date filter (dropdown: overdue, today, next 7 days, all)
  - [ ] Clear filters button

- [ ] Create `src/components/tasks/TaskStatusQuickChange.vue`:
  - [ ] Dropdown to change status inline
  - [ ] Radio buttons for TO_DO, IN_PROGRESS, DONE

### Notification Components
- [ ] Create `src/components/notifications/NotificationToast.vue`:
  - [ ] Display notification with task title, reason, due date
  - [ ] Acknowledge button
  - [ ] Auto-dismiss or manual close
  - [ ] Stack multiple toasts

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

- [ ] Create `src/pages/AuthPage.vue`:
  - [ ] Render `LoginPage` component
  - [ ] Redirect to home if already authenticated
  - [ ] Handle OAuth callback

- [ ] Create `src/pages/HomePage.vue`:
  - [ ] Layout: sidebar + main content
  - [ ] List sidebar showing all lists
  - [ ] Main area with task list or empty state
  - [ ] Render task list + filter bar
  - [ ] Create/edit task modals
  - [ ] Load initial data on mount

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

- [ ] Create `src/router/index.ts`:
  - [ ] Define routes: /, /auth, /home
  - [ ] Meta guards for authentication
  - [ ] Redirect unauthenticated users to /auth
  - [ ] Redirect authenticated users from /auth to /home

- [ ] Create `src/App.vue`:
  - [ ] Root component with router-view
  - [ ] Global notification toast container
  - [ ] Loading overlay for app-level loading

- [ ] Create `src/main.ts`:
  - [ ] Initialize Vue app
  - [ ] Setup Pinia
  - [ ] Setup Vue Query (TanStack Query)
  - [ ] Setup router
  - [ ] Setup WebSocket notifications
  - [ ] Mount to DOM

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
  - [ ] `validateDueDate(date)` - must be today or future
  - [ ] `getErrorMessage(field, error)` - user-friendly error messages

- [ ] Create error handling in API calls:
  - [ ] Display error toasts on API failures
  - [ ] Handle 401 Unauthorized → redirect to login
  - [ ] Handle 403 Forbidden → show permission error
  - [ ] Handle 404 Not Found → show "not found" message
  - [ ] Handle 400 Bad Request → show validation errors

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
  - [ ] Environment variables `.env` setup
  - [ ] OAuth callback URL configuration
  - [ ] Component architecture overview
  - [ ] Store structure documentation
  - [ ] API integration patterns
  - [ ] WebSocket message format
  - [ ] Testing instructions

- [ ] Create `COMPONENT_GUIDE.md`:
  - [ ] Component props, slots, emits
  - [ ] Usage examples
  - [ ] Common patterns

- [ ] Create `.env.example`:
  - [ ] `VITE_API_URL`
  - [ ] `VITE_OAUTH_GOOGLE_CLIENT_ID`
  - [ ] `VITE_OAUTH_GITHUB_CLIENT_ID`

- [ ] Setup build and deployment:
  - [ ] Build production: `npm run build`
  - [ ] Optimize bundle size
  - [ ] Setup CI/CD for frontend (GitHub Actions)

**Files to Create/Update**:
- [ ] Update `README.md`
- [ ] Create `COMPONENT_GUIDE.md`
- [ ] Create `.env.example`

---

## Local Development Verification

- [ ] Install dependencies: `npm install`
- [ ] Setup `.env.development` with local backend URL
- [ ] Start dev server: `npm run dev`
- [ ] Browser opens to http://localhost:5173
- [ ] Vite hot reload works
- [ ] API calls work (check Network tab in DevTools)

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
