# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Keep your answers short and meaningfull.

## Project Overview

This is a full-stack Personal To-Do Manager web application built as an MVP to demonstrate end-to-end product implementation. Users can manage tasks across multiple projects with SSO authentication, real-time WebSocket notifications, and task filtering/search capabilities.

**Key Constraint**: This is a single-user SPA (each user sees only their own data, no sharing).

## Tech Stack

**Backend**:
- Java 21
- Spring Boot 3.x
- Spring Security (with OAuth2 client)
- Gradle
- Spring WebSocket for real-time notifications
- Spring Data JPA for ORM

**Database**: MySQL 8.x

**Migrations**: Flyway

**Backend Tests**: JUnit 5, Mockito, AssertJ, TestContainers (integration tests)

**Frontend**:
- Vue 3
- Vite (dev server & build)
- Tailwind CSS + Headless UI (or shadcn equivalent)
- TanStack Query (Vue Query) for data fetching
- Pinia for state management
- Native WebSocket API

**Frontend Tests**: Vitest + @vue/test-utils (unit/component), Playwright (E2E)

See `/REQUIREMENTS.MD` for full feature requirements and acceptance criteria.

## Core Requirements Summary

### Authentication
- SSO only: Google OAuth & GitHub OAuth
- User profile persisted with provider, user_id, email, display_name, avatar_url

### Data Model
- **Lists/Projects**: CRUD operations
- **Tasks**: title, description, status (To Do | In Progress | Done), due_date, priority (Low | Medium | High)
- **Deletion**: Choose cascade or block-until-empty (document in README)

### Features
- Search by title & description
- Filters: status, priority, due date (overdue, today, next 7 days, all)
- **WebSocket**: Real-time notifications for overdue & due-soon tasks
  - Server → Client: push overdue/due-soon notifications
  - Client → Server: `subscribe` & `ack` message formats (define in README)
  - Send initial batch on connect; periodic checks while connected
- Responsive UI with sidebar, empty states, form validation feedback, loading states

### Quality
- Authorization enforcement on all list/task/WebSocket operations
- Input validation with clear error responses
- Tests covering: SSO login, list/task creation, cross-user isolation, WebSocket notifications
- Sample data (seed script, endpoint, or manual steps)
- README with local run, OAuth setup docs, WebSocket message formats, API description


## Build & Run Commands

### Backend (Gradle + Spring Boot)
```bash
# Build
gradle build

# Run tests
gradle test

# Run single test
gradle test --tests com.taskmanager.service.TaskServiceTest

# Start dev server
gradle bootRun

# Build JAR
gradle bootJar

# Run JAR
java -jar build/libs/app-0.0.1-SNAPSHOT.jar
```

### Frontend (Vue 3 + Vite)
```bash
# Setup
npm install

# Dev server (hot reload)
npm run dev

# Build for production
npm run build

# Run component/unit tests (Vitest)
npm run test

# Run single test
npm run test src/components/TaskForm.spec.ts

# Watch mode (reruns on file change)
npm run test:watch

# E2E tests (Playwright)
npx playwright test

# E2E single file
npx playwright test tests/auth.spec.ts

# View Playwright report
npx playwright show-report
```

## Key Implementation Notes

1. **Authorization**: Every API endpoint must verify the user owns the resource being accessed. No cross-user data leakage.

2. **WebSocket**: Define message schemas (at minimum `{ type: "subscribe", interval?: ms }` and `{ type: "ack", notificationId }`) in README before building.

3. **OAuth Flow**: Implement secure redirect + token exchange. Store provider + provider_user_id in user table to uniquely identify users.


4. **Notification Rules**:
   - **Overdue**: status ≠ Done AND due_date < today
   - **Due Soon**: status ≠ Done AND due_date within next 3 days (inclusive)
   - Send on connect + periodic checks (interval developer-defined)