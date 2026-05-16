# Personal To-Do Manager

## Task

Create a full-stack web app using Claude Code.

## Goal

Deliver a working MVP that demonstrates end-to-end product implementation:

*   frontend
*   backend
*   persistence
*   tests

The developer chooses the stack, database, UI library, and hosting approach.  During /init ask me about stack for Backend, Frontend, Persistance and tests. 

**Local run is required.**

## Business Scenario

A user manages personal work across multiple projects and wants a simple system to:

*   plan tasks
*   set priorities and deadlines
*   quickly see what requires attention today and this week

***

## 1. Core Rules

*   The system supports **multiple users**
*   Each user sees **only their own data**
*   There is **no sharing** (no invites, no public links, no shared lists)

***

## 2. Functional Requirements

### 2.1 Authentication and Accounts (SSO only)

*   Authentication via **SSO only**
*   Supported providers:
    *   Google OAuth (OpenID Connect)
    *   GitHub OAuth
*   Must support logout
*   Session must persist across page refresh

#### User profile (persisted in backend):

*   `provider` (google | github)
*   `provider_user_id`
*   `email` (if available)
*   `display_name`
*   `avatar_url` (optional)

***

### 2.2 Lists / Projects

Users can:

*   Create a list/project
*   Rename a list/project
*   Delete a list/project

Deletion behavior (choose one and document in README):

*   Cascade delete all tasks  
    **OR**
*   Block deletion until empty

***

### 2.3 Tasks

Each task belongs to exactly one list and includes:

*   `title`
*   `description`
*   `status`: To Do | In Progress | Done
*   `due_date`
*   `priority`: Low | Medium | High

#### Operations:

*   Create task
*   Edit task
*   Delete task
*   Quick status change from list view

***

### 2.4 Search and Filters

*   Search by **title and description**
*   Filter by:
    *   Status
    *   Priority
    *   Due date:
        *   overdue
        *   today
        *   next 7 days
        *   all

***

### 2.5 Real-Time Communication (WebSocket)

#### Requirements

Two-way communication via WebSocket is required.

***

#### Server → Client Notifications

While connected, backend must push:

*   **Overdue tasks**
    *   status ≠ Done
    *   due date < today

*   **Due soon tasks**
    *   status ≠ Done
    *   due date within next 3 days (inclusive)

#### UI:

*   Toast / banner / notification panel

***

#### Client → Server Messages

Client must send at least one meaningful message:

Examples:

*   `subscribe` (preferences: interval, types)
*   `ack` (mark notification as read)

Message formats must be described in README.

***

#### Scheduling Rules

*   On connect → send initial notification batch
*   While connected → periodic checks (interval defined by developer)

***

## 3. UI Requirements

### Core UI

*   Auth screen with:
    *   Continue with Google
    *   Continue with GitHub
*   Main layout:
    *   Sidebar → lists/projects
    *   Main area → tasks
*   Task list view
*   Task create/edit UI (modal / drawer / page)
*   Search + filters in task view
*   Notifications UI (WebSocket)
*   Responsive design (collapsible sidebar)

***

### "Modern UI" Criteria (Required)

*   Consistent spacing & typography
*   Hover and focus states
*   Empty states:
    *   no tasks
    *   no search results
*   At least one loading state
*   Client-side form validation feedback

***

## 4. Backend Requirements

*   Provide REST API (or equivalent)
*   Enforce authorization:
    *   on all list/task operations
    *   on WebSocket connections
*   Validate inputs + clear error responses
*   Implement secure OAuth flows

Document:

*   Google OAuth setup
*   GitHub OAuth setup
*   Required environment variables

***

## 5. Data Requirements

*   Persist data in a database
*   Provide sample data:
    *   seed script  
        **or**
    *   endpoint  
        **or**
    *   manual steps (documented)

***

## 6. Quality Requirements

Automated tests must cover:

*   SSO login success (mock provider)
*   Create list + task
*   Authorization isolation (no cross-user access)
*   WebSocket notifications:
    *   overdue
    *   due soon

***

## 7. Deliverables

*   Source code in GitLab repo
*   README with:
    *   local run instructions
    *   OAuth setup (Google + GitHub)
    *   WebSocket message formats
    *   notification rules
    *   API description (OpenAPI / Postman / README)
*   Runnable test command

***

### Optional

*   Docker / docker-compose  
    (if not possible → document)

***

## 8. Acceptance Checklist

*   User can log in via Google & GitHub
*   User can:
    *   create lists
    *   create tasks
    *   use search & filters
*   Data is private per user
*   Real-time notifications work
*   WebSocket includes client → server message
*   Tests pass locally
*   README is clear and complete