# Phase 5 (WebSocket Notifications) - Manual Verification Guide

**Date**: 2026-05-21  
**Purpose**: Manual testing procedures for WebSocket real-time notifications  
**Prerequisites**: Phase 1-4 complete, backend running, authenticated user logged in

---

## Overview

Phase 5 implements real-time WebSocket notifications for overdue and due-soon tasks. This guide provides step-by-step procedures to manually verify the WebSocket layer works correctly.

### Key Features to Test
- ✅ WebSocket connection establishment
- ✅ Initial notification batch on connect
- ✅ Client subscribe message handling
- ✅ Client acknowledge message handling
- ✅ Periodic notifications (60s interval)
- ✅ Authorization enforcement (session-based)
- ✅ User isolation (no cross-user leakage)

---

## Setup & Prerequisites

### 1. Backend Setup
```bash
cd backend
./gradlew bootRun
```
- Backend should run on `http://api.localhost:8080` (or `http://localhost:8080` in Docker)
- MySQL should be running with task_manager database
- All Phase 1-4 migrations should be applied

### 2. Create Test Data
```bash
# Option A: Via Seed Endpoint (if implemented)
POST http://localhost:8080/api/seed
# Creates test user, lists, and tasks with various due dates

# Option B: Manual Setup
1. Login with Google/GitHub OAuth
2. Create a list: POST /api/lists with title "Test List"
3. Create tasks with various due dates:
   - Overdue task: due_date = 2026-05-18 (3 days ago)
   - Due-soon task: due_date = 2026-05-22 (tomorrow)
   - Not due: due_date = 2026-06-01 (future)
```

### 3. WebSocket Client Tools

**Option A: Browser Console with STOMP (Recommended)**
```javascript
// Load STOMP library
const script = document.createElement('script');
script.src = 'https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js';
document.head.appendChild(script);

// Wait for library to load, then connect
setTimeout(() => {
    const ws = new WebSocket('ws://localhost:8080/api/ws');
    const stompClient = Stomp.over(ws);

    stompClient.debug = (msg) => console.log('[STOMP DEBUG]:', msg);                                                                                                                                                 
   
    stompClient.connect({}, function(frame) {                                                                                                                                                                        
      console.log('✅ STOMP Connected:', frame);                                                                                                                                                                   
      // REQUIRED initial Subscription as first call after connect!!!
      // Only subscribe AFTER connection confirmed
      console.log('📌 About to subscribe...');
      const subscription = stompClient.subscribe('/app/notifications', function(msg) {
        console.log('📨 Message received:', JSON.parse(msg.body));
      });
      console.log('📌 Subscription object:', subscription);
    }, function(error) {
      console.error('❌ Connection error:', error);
    });


    // LISTEN TO USER PRIVATE QUEUE
   stompClient.subscribe('/user/queue/notifications', (message) => {
      console.log("Notifications received:", JSON.parse(message.body));
   });

  }, 1000);

```

**Option B: wscat CLI Tool**
```bash
npm install -g wscat
wscat -c ws://api.localhost:8080/api/ws
```

**Option C: Postman WebSocket (if available)**
- New → WebSocket Request
- URL: `ws://api.localhost:8080/api/ws`

**Option D: Custom Test Script (Node.js)**
```javascript
const WebSocket = require('ws');
const ws = new WebSocket('ws://api.localhost:8080/api/ws', {
  headers: { Cookie: 'your_session_cookie' }
});

ws.on('open', () => console.log('Connected'));
ws.on('message', msg => console.log('Notification:', msg));
ws.on('error', err => console.error('Error:', err));
```

### STOMP Routing Explained

**Path types in STOMP**:

1. **`/app/*` destinations** (Client → Server commands only)
   - Used for `@MessageMapping` handlers (e.g., `/app/subscribe`, `/app/ack`)
   - Client SENDS: `stompClient.send('/app/subscribe', ...)`
   - Server receives and processes the command
   - ❌ NOT for receiving messages

2. ** BROKER DESTINATIONS: `/queue/*` `/topic/*`. Client should subscribe to get messages. Broker sends.

3. **`/queue/*` destinations** (Handler → Broker → Client messaging)
   - Used for public messages
   - Used for `messagingTemplate.convertAndSend()`

4. **`/user/*` prefix** (Spring's automatic per-user routing)
   - Configured in `WebSocketConfig`: `config.setUserDestinationPrefix("/user")`
   - Spring automatically adds the authenticated user's ID to the path
   - Client subscribes: `stompClient.subscribe('/user/queue/notifications', ...)`
   - Spring converts internally to: `/user/{authenticated-user-id}/queue/notifications`
   - Broker routes back to specific user's session only
   - **Purpose**: Each user only receives their own messages (isolation + security)
   - Backend send: `messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", msg)` -> client needs second subscription to `/user/queue/notifications` which will transformed by Spring in `/user/{authenticated-user-id}/queue/notifications`
   - **Important**: Only `/user` is in broker's `enableSimpleBroker()`, so `/queue/*` routes to handlers first

4. **`@SubscribeMapping` used to return response immediately. Inital data
   - if called in method `messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", msg)` will be sending response into channel also but user need to subscirbe for it additionaly. `@SubscribeMapping("/notifications")` it's initial request to get data.

**Broker configuration**:
```java
config.enableSimpleBroker("/user");  // Only /user in broker
```
- `/user/*/queue/` destinations go to broker (for user-specific routing)
- `/queue/*` destinations global

---

## Test Cases

### Test 1: WebSocket Connection Establishment
**Goal**: Verify WebSocket endpoint is accessible and accepts connections

**Steps**:
1. Start backend: `./gradlew bootRun`
2. Login via OAuth (Google/GitHub) to establish session
3. Open browser DevTools → Network tab
4. Run JavaScript in console (see Option A above for full STOMP setup):
   ```javascript
   const ws = new WebSocket('ws://localhost:8080/api/ws');
   const stompClient = Stomp.over(ws);
   stompClient.connect({}, 
     () => console.log('✅ Connected'),
     (error) => console.log('❌ Failed:', error)
   );
   ```
5. Check Network tab for WebSocket (WS) connection with status "101 Switching Protocols"

**Expected Result**:
- ✅ WebSocket connection established
- ✅ Status shows "101 Switching Protocols"
- ✅ Connection persists (doesn't close immediately)

**Failure Cases**:
- ❌ "Connection refused" → Backend not running
- ❌ "401 Unauthorized" → Not authenticated (need OAuth login first)
- ❌ "404 Not Found" → Wrong endpoint path

---

### Test 2: Initial Notification Batch on Connect
**Goal**: Verify server sends overdue/due-soon tasks immediately when client connects

**Setup**:
1. Create test tasks with different due dates:
   - Task A: due_date = TODAY - 3 days, status = "TO_DO" (overdue)
   - Task B: due_date = TODAY + 1 day, status = "TO_DO" (due soon)
   - Task C: due_date = TODAY + 10 days, status = "TO_DO" (not due)
   - Task D: due_date = TODAY - 1 day, status = "DONE" (done tasks excluded)

**Steps**:
1. Connect to WebSocket with STOMP client
2. Within 1 second, you should receive 2 notifications (for Task A and B)
3. Check console for notification payload:
   ```javascript
   stompClient.subscribe('/user/queue/notifications', function(msg) {
     const notification = JSON.parse(msg.body);
     console.log('✅ Received:', {
       id: notification.id,                  // UUID
       taskId: notification.taskId,          // UUID
       title: notification.title,            // "Task A"
       reason: notification.reason,          // "overdue" or "due-soon"
       dueDate: notification.dueDate,        // "2026-05-18"
       priority: notification.priority       // "HIGH", "MEDIUM", "LOW"
     });
   });
   ```

**Expected Result**:
- ✅ Exactly 2 notifications received (overdue + due-soon)
- ✅ Task C not included (future date)
- ✅ Task D not included (status=DONE)
- ✅ Each notification has all required fields
- ✅ `reason` field is either "overdue" or "due-soon"

**Failure Cases**:
- ❌ No notifications received → Server not sending initial batch
- ❌ More/fewer than 2 notifications → Query logic incorrect
- ❌ Missing fields in notification → DTO mapping incomplete
- ❌ DONE task included → Status filter not working

---

### Test 3: Client Subscribe Message (Change Interval)
**Goal**: Verify client can change polling interval dynamically

**Steps**:
1. Connect to WebSocket with STOMP client (see Option A above)
2. Send subscribe message with custom interval:
   ```javascript
   stompClient.send('/app/subscribe', {}, JSON.stringify({
     interval: 5000  // 5 seconds instead of default 60s
   }));
   ```
3. Wait and observe: should receive periodic notifications every ~5 seconds
4. Change interval again:
   ```javascript
   stompClient.send('/app/subscribe', {}, JSON.stringify({
     interval: 15000  // 15 seconds
   }));
   ```
5. Wait and observe: interval should change to ~15 seconds

**Expected Result**:
- ✅ Server accepts subscribe message
- ✅ Interval changes reflect new rate
- ✅ No error response from server
- ✅ Connection remains open

**Failure Cases**:
- ❌ No acknowledgment received → Message not parsed
- ❌ Interval doesn't change → Scheduler not updated
- ❌ Connection closes → Error in handler

---

### Test 4: Client Acknowledge Message
**Goal**: Verify client can acknowledge notifications

**Steps**:
1. Connect with STOMP and receive initial notification batch
2. Extract a `id` from first notification payload
3. Send acknowledge message:
   ```javascript
   stompClient.send('/app/ack', {}, JSON.stringify({
     notificationId: 'uuid-from-notification'
   }));
   ```
4. Verify backend logs: `User [userId] acknowledged notification with id [id]`
5. Verify subsequent periodic checks don't re-send same notification

**Expected Result**:
- ✅ Server accepts ack message
- ✅ Notification not re-sent on next periodic check
- ✅ No error response

**Failure Cases**:
- ❌ No response to ack → Handler not processing message
- ❌ Same notification re-sent → Ack not tracked

---

### Test 5: Periodic Notifications (60s Interval)
**Goal**: Verify scheduler sends new notifications periodically

**Steps**:
1. Connect and record initial notifications
2. Create a new task: title="Urgent", due_date = TODAY (makes it "due-soon")
3. Wait up to 60 seconds (default interval)
4. New notification should arrive in console
5. Verify:
   - ✅ Notification received within ~60s
   - ✅ Notification is for new task
   - ✅ Previously sent notifications NOT re-sent

**Expected Result**:
- ✅ New task notification received
- ✅ Within 60-65 second window (accounting for scheduler jitter)
- ✅ Previous tasks not duplicated

**Failure Cases**:
- ❌ No notification after 70s → Scheduler not running
- ❌ Duplicate notifications → Deduplication not working
- ❌ Old tasks keep appearing → State not cleared per cycle

---

### Test 6: Authorization Enforcement
**Goal**: Verify unauthenticated users are rejected

**Steps**:
1. **Without Session**: Open new private/incognito browser window (no cookies)
2. Run:
   ```javascript
   const ws = new WebSocket('ws://localhost:8080/api/ws');
   const stompClient = Stomp.over(ws);
   stompClient.connect({},
     () => console.log('❌ Connected (SECURITY ISSUE!)'),
     (error) => console.log('✅ Connection rejected:', error)
   );
   ```
3. Connection attempt should fail immediately with 401/403 error

**Expected Result**:
- ❌ Connection rejected (401 Unauthorized)
- ❌ Error event fires immediately
- ❌ No notifications sent

**Failure Cases**:
- ✅ Connection accepted → Security vulnerability! Unauthenticated access allowed
- ✅ Notifications received → Data leakage!

---

### Test 7: User Isolation (No Cross-User Leakage)
**Goal**: Verify User A doesn't receive User B's notifications

**Setup**:
1. Create two test accounts (User A & User B) via OAuth
2. User A: Create task T1 (overdue)
3. User B: Create task T2 (overdue)

**Steps**:
1. Login as **User A** → Connect WebSocket → Record notifications (should see T1 only)
2. Login as **User B** → Connect WebSocket → Record notifications (should see T2 only)
3. Verify:
   - ✅ User A's WebSocket shows only T1
   - ✅ User B's WebSocket shows only T2
   - ✅ No cross-user task IDs

**Expected Result**:
- ✅ Each user sees only their own tasks
- ✅ Complete isolation

**Failure Cases**:
- ❌ User A receives User B's notifications → Authorization bypassed
- ❌ Cross-user task IDs visible → Data leakage vulnerability

---

### Test 8: Overdue Task Detection
**Goal**: Verify overdue tasks are correctly identified

**Steps**:
1. Create task with due_date = TODAY - 5 days, status = "TO_DO"
2. Connect WebSocket → should receive notification with `reason: "overdue"`
3. Update task status to "DONE"
4. Reconnect WebSocket → notification should NOT appear (done tasks excluded)

**Expected Result**:
- ✅ Overdue notification received with correct reason
- ✅ Done task excluded

**Failure Cases**:
- ❌ Reason shows "due-soon" → Date math incorrect
- ❌ Done task still appears → Status filter broken

---

### Test 9: Due-Soon Task Detection (Configurable Window)
**Goal**: Verify due-soon window respects configuration (default 3 days)

**Steps**:
1. Create tasks:
   - Task A: due_date = TODAY + 1 day (should be due-soon)
   - Task B: due_date = TODAY + 3 days (should be due-soon, inclusive)
   - Task C: due_date = TODAY + 4 days (should NOT be due-soon)
   - Task D: due_date = TODAY (should be due-soon, today included)
2. Connect WebSocket → verify only A, B, D appear with `reason: "due-soon"`

**Expected Result**:
- ✅ Tasks within 3-day window included
- ✅ Tasks beyond 3 days excluded
- ✅ Window is inclusive of TODAY and TODAY+3

**Failure Cases**:
- ❌ Task C included → Window calculation wrong
- ❌ Task D excluded → TODAY not included

---

### Test 10: Connection Persistence
**Goal**: Verify WebSocket connection stays open and keeps receiving updates

**Steps**:
1. Connect WebSocket
2. Keep connection open for 5+ minutes
3. Monitor: connection should remain active
4. At 60s marks, new notifications should arrive
5. Close connection intentionally:
   ```javascript
   ws.close();
   ```

**Expected Result**:
- ✅ Connection remains open
- ✅ Periodic notifications arrive at intervals
- ✅ Connection closes cleanly when `close()` called
- ✅ No spurious disconnections

**Failure Cases**:
- ❌ Connection drops spontaneously → Keep-alive issue
- ❌ No notifications after 1-2 minutes → Scheduler failure
- ❌ Connection won't close → Resource leak

---

## Automated Test Verification

### Run Unit Tests
```bash
cd backend
./gradlew test --tests com.taskmanager.websocket.*
```

**Expected**:
- ✅ TaskNotificationHandlerTest: 15+ tests passing
- ✅ NotificationSchedulerTest: 10+ tests passing
- ✅ Total: 25+ WebSocket tests

### Run All Tests
```bash
./gradlew test
```

**Expected**:
- ✅ Overall: 65+ tests passing (40 from Phase 1-4 + 25 new WebSocket)
- ✅ No failures

### Build Verification
```bash
./gradlew build
```

**Expected**:
- ✅ BUILD SUCCESSFUL
- ✅ No compilation errors
- ✅ No checkstyle violations

---

## Checklist for Phase 5 Sign-Off

| Test | Status | Notes |
|------|--------|-------|
| WebSocket connection | [ ] | Can connect to `ws://api.localhost:8080/api/ws` |
| Initial batch | [ ] | Receive overdue + due-soon tasks on connect |
| Subscribe message | [ ] | Can change interval with subscribe message |
| Acknowledge message | [ ] | Can acknowledge notifications with ack message |
| Periodic updates | [ ] | New notifications arrive every ~60s |
| Authorization | [ ] | Unauthenticated connections rejected (401) |
| User isolation | [ ] | Users only see their own tasks |
| Overdue detection | [ ] | Overdue reason correctly identified |
| Due-soon detection | [ ] | Due-soon window respected (3 days) |
| Connection persistence | [ ] | Connection stays open, survives 5+ minutes |
| Unit tests pass | [ ] | 25+ WebSocket tests passing |
| Integration tests pass | [ ] | All 65+ tests passing |
| Build succeeds | [ ] | `gradle build` completes successfully |

---

## Troubleshooting

### WebSocket Connection Refused
**Symptom**: `Connection refused` or `ECONNREFUSED`  
**Cause**: Backend not running  
**Fix**: Start backend with `./gradlew bootRun`

### 401 Unauthorized
**Symptom**: Connection closes immediately with 401  
**Cause**: Not authenticated  
**Fix**: Login via Google/GitHub OAuth first, ensure session cookie exists

### No Notifications Received
**Symptom**: Connected but no messages  
**Cause**: No overdue/due-soon tasks, or scheduler not running  
**Fix**: Create a task with due_date = TODAY or yesterday

### Duplicate Notifications
**Symptom**: Same task notification appears multiple times  
**Cause**: Deduplication not working  
**Fix**: Verify scheduler is tracking sent notifications correctly

### Connection Drops After ~30s
**Symptom**: WebSocket closes unexpectedly  
**Cause**: Timeout or keep-alive issue  
**Fix**: Ensure browser keeps connection active, check for browser extensions blocking WebSocket

---

## Manual Test Report Template

```markdown
# Phase 5 Manual Test Results

**Tester**: [Name]  
**Date**: 2026-05-XX  
**Environment**: [Local / Docker]  
**Browser**: [Chrome / Firefox / Safari]

## Results

| Test | Pass | Notes |
|------|------|-------|
| Test 1: Connection | [ ] | |
| Test 2: Initial Batch | [ ] | |
| Test 3: Subscribe | [ ] | |
| Test 4: Acknowledge | [ ] | |
| Test 5: Periodic | [ ] | |
| Test 6: Authorization | [ ] | |
| Test 7: Isolation | [ ] | |
| Test 8: Overdue | [ ] | |
| Test 9: Due-Soon | [ ] | |
| Test 10: Persistence | [ ] | |

## Overall Status
- [ ] ✅ ALL TESTS PASSED - Phase 5 Ready
- [ ] ❌ FAILED - Issues found (see notes)

## Issues Found
[List any failures]

## Sign-Off
Phase 5 verified on [date]: [Approved/Not Approved]
```

---

## Next Steps

After Phase 5 verification:
1. ✅ Commit WebSocket implementation
2. ✅ Update README with WebSocket docs
3. → Phase 6: Polish & Deployment

**Success Criteria**: All 10 manual tests pass + 25+ unit tests passing + `gradle build` successful
