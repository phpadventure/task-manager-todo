# Phase 5 - Pre-Implementation Tasks

## Overview
This document outlines critical blockers that must be resolved before Phase 5 implementation begins. **NOTE**: Phase 4 manual testing is currently failing and must be fixed first.

---

## Blocker: Complete Phase 4 Manual Testing

**Priority**: CRITICAL  
**Status**: In Progress  
**Issue**: Phase 4 functionality is not working

### Description
Phase 4 manual testing suite is currently failing. All issues must be identified and fixed before Phase 5 implementation can proceed.

### Objectives
- Run Phase 4 manual tests (see PHASE4_MANUAL_TESTS.md)
- Identify which features are broken
- Debug and fix issues
- Re-run tests to verify functionality restored

### Acceptance Criteria
- All Phase 4 manual tests passing
- Core functionality (Task CRUD, Filters, WebSocket, etc.) working as expected
- No data loss or corruption

---

## Task 1: Investigate Database Pruning on Application Startup

**Priority**: High  
**Status**: Pending

### Description
Debug and fix the database pruning behavior that occurs when the Spring Boot application starts.

### Objectives
- Determine if data pruning is intentional (seed data, migration) or a bug
- Identify which component/process is responsible for the pruning
- Review Flyway migrations, database initialization, or data setup code
- Check if related to test fixtures or sample data loader
- Document findings and implement fix if necessary

### Acceptance Criteria
- Root cause identified and documented
- Behavior verified as either intentional or fixed
- Data persistence confirmed across app restarts

---

## Task 2: Refactor Authorization Checks to Use @Secured Annotation

**Priority**: High  
**Status**: Pending

### Description
Replace hardcoded authorization checks throughout the codebase with Spring Security's `@Secured` annotation for cleaner, declarative security.

### Objectives
- Audit current authorization logic in controllers and services
- Identify all hardcoded permission checks
- Convert to `@Secured` annotation on methods
- Enable `@EnableGlobalMethodSecurity` in SecurityConfig if needed
- Verify all authorization logic still functions correctly

### Acceptance Criteria
- All hardcoded authorization checks replaced with `@Secured`
- Tests passing for all secured endpoints
- Authorization still enforced correctly (no cross-user data leakage)
- Code cleaner and more maintainable

---

## Task 3: Audit Enum Validation in REST Calls and Database Storage

**Priority**: Medium  
**Status**: Pending

### Description
Verify that enums (TaskStatus, TaskPriority) are properly validated on REST request inputs and correctly persisted in the database.

### Objectives
- Review enum validation in DTOs (CreateTaskRequest, UpdateTaskRequest)
- Check REST endpoint input validation for invalid enum values
- Verify error responses for invalid enum input
- Audit database storage for enum consistency
- Test edge cases (null, invalid string, etc.)

### Acceptance Criteria
- Invalid enum values rejected with clear error message
- Valid enum values correctly persisted in DB
- Enum retrieval from DB works correctly
- Edge cases tested and handled appropriately
- Error responses documented in API specs

---

## Implementation Order
0. **BLOCKER**: Fix Phase 4 manual testing failures (required before proceeding)
1. Task 1 (DB pruning) - unblocks further development
2. Task 2 (Authorization refactor) - improves code quality
3. Task 3 (Enum validation) - data consistency improvement

## Next Steps
1. Debug and fix Phase 4 failures
2. Verify all Phase 4 tests passing
3. Then proceed with Tasks 1-3 for Phase 5 implementation
