# Phase 5 - Pre-Implementation Tasks

## Overview
This document outlines critical blockers that must be resolved before Phase 5 implementation begins. **NOTE**: Phase 4 manual testing is currently failing and must be fixed first.

---

## Blocker: Complete Phase 4 Manual Testing

**Priority**: CRITICAL  
**Status**: ✅ COMPLETE  
**Issue**: Phase 4 functionality is not working

### Description
Phase 4 manual testing suite has been completed successfully. All issues identified and fixed. Phase 5 implementation can now proceed.

### Objectives
- Run Phase 4 manual tests (see PHASE4_MANUAL_TESTS.md) ✅
- Identify which features are broken ✅
- Debug and fix issues ✅
- Re-run tests to verify functionality restored ✅

### Acceptance Criteria
- All Phase 4 manual tests passing ✅
- Core functionality (Task CRUD, Filters, WebSocket, etc.) working as expected ✅
- No data loss or corruption ✅

---

## Task 1: Investigate Database Pruning on Application Startup

**Priority**: High  
**Status**: ✅ FIXED

### Description
Database pruning behavior on Spring Boot startup has been debugged and fixed. Data now persists correctly across application restarts.

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

## Task 4: Review and Improve Task List API Endpoint Design

**Priority**: Medium  
**Status**: Pending

### Description
Review the current task list endpoint to ensure it follows RESTful conventions. Currently using query parameters approach; evaluate if nested resource approach would be more appropriate.

### Objectives
- Check SPEC.md for intended API design (currently: `GET /api/tasks?list_id={id}&status={status}&...`)
- Evaluate RESTful alternatives (e.g., `GET /api/lists/{listId}/tasks`)
- Consider backward compatibility if endpoints are in use
- Document design decision (why query params vs. nested resources)
- Update if needed to improve clarity and consistency

### Acceptance Criteria
- API design rationale documented
- Endpoints follow consistent RESTful patterns
- Implementation matches SPEC.md requirements or updated SPEC.md reflects design
- No breaking changes without migration plan

---

## Implementation Order
0. **BLOCKER**: Fix Phase 4 manual testing failures (required before proceeding)
1. Task 1 (DB pruning) - unblocks further development
2. Task 2 (Authorization refactor) - improves code quality
3. Task 4 (API endpoint design) - ensure RESTful conventions
4. Task 3 (Enum validation) - data consistency improvement

## Next Steps
1. Debug and fix Phase 4 failures
2. Verify all Phase 4 tests passing
3. Then proceed with Tasks 1-3 for Phase 5 implementation
