# Error Handling Implementation Summary

## Overview
Comprehensive error handling has been implemented for API validation failures when creating/updating tasks and lists. Errors are now extracted from backend responses, displayed to users via error toasts, and shown in form alerts.

## What Was Implemented

### 1. **Error Extraction Layer** (`src/utils/apiErrorHandler.ts`)
- `getErrorMessage(error)`: Extracts user-friendly messages from AxiosError responses
  - Tries: backend `error` field → `details.message` → status code message → generic fallback
  - Returns: `{ message, statusCode?, details? }`
- `getStatusCodeMessage(statusCode, context?)`: Maps HTTP status codes to context-aware messages
  - 400: Validation failed
  - 401: Session expired
  - 403: Permission denied
  - 404: Not found (context-aware: "task/list not found")
  - 409: Conflict (e.g., "Cannot delete list with tasks")
  - 500: Server error
- Helper predicates: `isConflictError()`, `isValidationError()`, `isNotFoundError()`, `isAuthError()`

### 2. **Toast Error Display** (`src/composables/useToastError.ts`)
- `showError(error, options?)`: Display error toast with extracted message
  - Falls back to fallback message only for generic errors
  - Auto-dismisses in 5 seconds (configurable)
- `showErrorWithContext(error, context?, options?)`: Context-aware error messages
  - Adds context label ("task"/"list") to generic messages
  - Useful for distinguishing error types

### 3. **Enhanced Notification Store** (`src/stores/notificationStore.ts`)
- `addErrorNotification(notification)`: New method to add error toasts
  - Supports both task notifications (existing) and error toasts (new)
  - Generates unique IDs with timestamp + random suffix
  - Auto-dismisses after configured duration (default 5 seconds)

### 4. **Composable Integration** (`src/composables/useTasks.ts`, `useLists.ts`)
- Updated all CRUD operations to call `showErrorWithContext()` before throwing
- Preserves existing error storage in Pinia stores
- Error toasts show automatically; errors also stored for form display

### 5. **Form Component Enhancements**
- **CreateTaskForm.vue**
  - Added error alert box below the title
  - Shows backend error message on submission failure
  - Catches and displays errors without closing modal
- **EditTaskForm.vue**
  - Separate error alerts for update and delete operations
  - Delete error shown in delete confirmation view
  - Update error shown in form view
- **CreateListModal.vue**
  - Error alert box for list creation failures
- **EditListModal.vue**
  - Separate error alerts for update and delete list operations

### 6. **Notification Toast Component** (`src/components/notifications/NotificationToast.vue`)
- Updated to handle both task notifications and error toasts
- Conditionally renders:
  - Error toasts using `Toast` component (generic error display)
  - Task notifications using custom card layout (with badges, due date, actions)
- Type-safe with TypeScript discriminated unions

### 7. **Tests** (24 tests, all passing)
- `src/utils/__tests__/apiErrorHandler.spec.ts`: 18 tests
  - Error message extraction from various response formats
  - Status code message generation
  - Error type predicates (conflict, validation, not found, auth)
- `src/composables/__tests__/useToastError.spec.ts`: 6 tests
  - Error display with extracted messages
  - Backend error messages override fallbacks
  - Context-aware messages
  - Custom durations

## Error Flow

```
API Call Fails
  ↓
Composable (useTasks/useLists) catches error
  ↓
1. Store error in Pinia store (for form display)
  ↓
2. Call showErrorWithContext() → Toast shown automatically
  ↓
3. Throw error to component
  ↓
Component catches error (CreateTaskForm/EditTaskForm/etc)
  ↓
Show form-level error alert (red box)
  ↓
Do NOT close modal (user can retry)
```

## Error Messages by Type

### 400 Bad Request (Validation)
- Backend: `{ error: "Title must not be empty" }`
- Display: "Title must not be empty" (form alert + toast)

### 404 Not Found
- Backend: `{ error: "Task not found or has been deleted" }`
- Display: "Task not found or has been deleted" (toast)

### 409 Conflict (Delete with constraints)
- Backend: `{ error: "Cannot delete list with active tasks", details: { message: "Please delete all tasks first" } }`
- Display: "Cannot delete list with active tasks" (toast + delete confirmation)

### 500 Server Error
- Backend: `{ error: "Database connection failed" }`
- Display: "Database connection failed" (toast)

## Files Created
1. `src/utils/apiErrorHandler.ts` - Error extraction utilities
2. `src/composables/useToastError.ts` - Toast error display composable
3. `src/utils/__tests__/apiErrorHandler.spec.ts` - 18 tests
4. `src/composables/__tests__/useToastError.spec.ts` - 6 tests

## Files Modified
1. `src/stores/notificationStore.ts` - Added error notification support
2. `src/composables/useTasks.ts` - Integrated error toasts
3. `src/composables/useLists.ts` - Integrated error toasts
4. `src/components/tasks/CreateTaskForm.vue` - Form error display
5. `src/components/tasks/EditTaskForm.vue` - Form error display
6. `src/components/lists/CreateListModal.vue` - Form error display
7. `src/components/lists/EditListModal.vue` - Form error display
8. `src/components/notifications/NotificationToast.vue` - Dual notification types

## Build & Test Status
✅ Build successful (no errors, 213 modules)
✅ 24 new tests passing (apiErrorHandler + useToastError)
✅ All form components compile successfully
✅ Notification store and components fully integrated

## Testing Checklist (Manual)
- [ ] Create task with validation error → see form alert + toast
- [ ] Update task with 404 error → see update error + toast
- [ ] Delete task with API error → see delete error message
- [ ] Try to delete list with tasks → see 409 conflict + error toast
- [ ] Session timeout (401) → redirect to login + error toast
- [ ] Network error (500) → see server error message + toast

## Session Persistence
- Error toasts auto-dismiss after 5 seconds (configurable via `notificationStore.ts`)
- Form error alerts persist until user corrects input or closes modal
- Users can manually dismiss error toasts with close button
