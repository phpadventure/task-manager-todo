# Phase 2 Authentication - Verification Guide

This guide provides step-by-step instructions to verify the Phase 2 Authentication implementation.

---

## Prerequisites

- Backend running on `http://localhost:8080`
- MySQL database configured and running
- Google OAuth credentials (Client ID & Secret)
- GitHub OAuth credentials (Client ID & Secret)
- `curl` command-line tool or Postman

---

## Setup: OAuth Credentials

### Google OAuth Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or use existing)
3. Enable **OAuth 2.0 Consent Screen**:
   - Navigate to "APIs & Services" → "OAuth consent screen"
   - Select "External" user type
   - Fill required fields (App name, User support email, etc.)
   - Add scopes: `profile` and `email`
4. Create **OAuth 2.0 Client ID**:
   - Go to "Credentials" → "Create Credentials" → "OAuth client ID"
   - Choose "Web application"
   - Add authorized redirect URI: `http://localhost:8080/api/login/oauth2/code/google`
   - Copy **Client ID** and **Client Secret**

### GitHub OAuth Setup

1. Go to GitHub Settings → [Developer settings → OAuth Apps](https://github.com/settings/developers)
2. Click "New OAuth App"
3. Fill in:
   - **Application name**: Task Manager (dev)
   - **Homepage URL**: `http://localhost:3000`
   - **Authorization callback URL**: `http://localhost:8080/api/login/oauth2/code/github`
4. Copy **Client ID** and **Client Secret**

---

## Step 1: Configure Environment Variables

Create or update `.env` in `/backend`:

```bash
# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# GitHub OAuth
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# Database (use defaults or override)
SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/task_manager
SPRING_DATASOURCE_USERNAME=taskuser
SPRING_DATASOURCE_PASSWORD=taskpass
```

Load environment variables in current shell:
```bash
cd backend
source .env
```

---

## Step 2: Start Backend Server

```bash
cd backend
./gradlew bootRun
```

Wait for output:
```
Started Application in X.XXX seconds (JVM running for X.XXX)
```

---

## Step 3: Verify Health Check

Test basic API availability:

```bash
curl http://localhost:8080/api/health
```

**Expected response (200)**:
```json
{"status":"UP","message":"Task Manager API is running"}
```

---

## Step 4: View API Documentation

Open browser to Swagger UI:
```
http://localhost:8080/api/swagger-ui.html
```

You should see:
- OpenAPI documentation
- Auth endpoints listed
- Health check endpoint
- Try-it-out buttons for each endpoint

---

## Step 5: Test Unauthenticated Access

Test `/me` endpoint without authentication:

```bash
curl http://localhost:8080/api/auth/me
```

**Expected response (401)**: Empty body with HTTP 401 status

---

## Step 6: Test OAuth Login (Google)

### Option A: Browser-based Swagger UI

1. Open `http://localhost:8080/api/swagger-ui.html`
2. Click "Authorize" button (top-right)
3. In modal, check "oauth2" checkbox
4. Click "Authorize" → redirected to Google login
5. Sign in with Google account
6. Grant permissions
7. Redirected back to Swagger UI (authenticated)
8. Click "Try it out" on `/api/auth/me` endpoint
9. Execute request

**Expected response (200)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "your-email@gmail.com",
  "displayName": "Your Name",
  "avatarUrl": "https://lh3.googleusercontent.com/...",
  "createdAt": "2026-05-18T14:30:00Z"
}
```

### Option B: Direct Browser Navigation

1. Open `http://localhost:8080/api/oauth2/authorization/google`
2. Sign in with Google
3. You'll be redirected with authenticated session

---

## Step 7: Test OAuth Login (GitHub)

### Option A: Browser-based Swagger UI

1. Logout first (see Step 10)
2. Refresh Swagger UI page: `http://localhost:8080/api/swagger-ui.html`
3. Click "Authorize" button
4. Check "oauth2" checkbox
5. Click "Authorize" → redirected to GitHub login
6. Sign in with GitHub
7. Authorize app
8. Back to Swagger, execute `/me` endpoint

**Expected response (200)**: Same structure as Google, but GitHub email/name

### Option B: Direct Browser Navigation

1. Open `http://localhost:8080/api/oauth2/authorization/github`
2. Sign in with GitHub
3. Authenticate with 2FA if enabled
4. Redirected with authenticated session

---

## Step 8: Verify Session Persistence

**Test**: Session survives page refresh

1. Login via Google or GitHub (using browser)
2. Open browser DevTools → Application → Cookies → `localhost:8080`
3. Note the `JSESSIONID` cookie
4. Close the Swagger UI tab
5. Open new tab, navigate to `http://localhost:8080/api/swagger-ui.html`
6. Execute `/api/auth/me` endpoint → **should return your profile**

Session persisted without re-authenticating. ✓

---

## Step 9: Test Logout

```bash
curl -X POST http://localhost:8080/api/logout
```

**Expected response (200)**: Empty body

Verify session cleared:
```bash
curl http://localhost:8080/api/auth/me
```

**Expected response (401)**

---

## Step 10: Test Multi-Provider Linking

**Objective**: Same user logs in with different OAuth providers using same email

### Scenario:

1. **Step 1**: Login with Google using `test@example.com`
   - Note the UUID in `/me` response (e.g., `550e8400-...`)
2. **Step 2**: Logout
3. **Step 3**: Login with GitHub using **same email** `test@example.com`
   - Call `/me` endpoint
   - **Should return same UUID** as Google login

**Result**: Same user account linked to both providers ✓

---

## Step 11: Test Cross-User Isolation

**Objective**: Verify user A cannot access user B's data

1. **User A**: Login with Google
   - Call `/api/auth/me` → returns User A's profile
2. **User A**: Open DevTools, copy `JSESSIONID` cookie value
3. **User B**: Login with GitHub in different browser (or incognito)
   - Call `/api/auth/me` → returns User B's profile
4. **User A**: Clear session, paste User B's `JSESSIONID` 
   - Call `/api/auth/me` → should NOT return User B's data (authorization check)

**Expected**: Each user sees only their own data ✓

---

## Step 12: Run Automated Tests

Verify all Phase 2 tests pass:

```bash
cd backend
./gradlew test
```

**Expected output**:
```
> Task :test
...
BUILD SUCCESSFUL in X.XXXs
```

Test classes that should pass:
- `UserRepositoryTest.java`
- `UserServiceTest.java`
- `AuthServiceTest.java`
- `AuthControllerTest.java`

---

## Step 13: Run Full Build with Code Quality Checks

```bash
cd backend
./gradlew build
```

**Expected output**:
```
> Task :checkstyleMain
> Task :checkstyleTest
> Task :jacocoTestReport
> Task :build

BUILD SUCCESSFUL in X.XXXs
```

Verifies:
- ✓ Checkstyle compliance (Google Java Style)
- ✓ All tests passing
- ✓ Code coverage report generated

---

## Step 14: Verify OpenAPI Documentation

### Swagger UI

Open `http://localhost:8080/api/swagger-ui.html` and verify:
- ✓ Title: "Personal To-Do Manager API"
- ✓ Version: "1.0.0"
- ✓ Description mentions OAuth2 and task management
- ✓ Auth endpoints documented:
  - `GET /api/auth/me`
  - `POST /api/auth/logout`
- ✓ Health endpoint documented:
  - `GET /api/health`

### OpenAPI JSON

```bash
curl http://localhost:8080/api/api-docs | jq .
```

**Verify**:
- `openapi: "3.0.1"`
- `info.title: "Personal To-Do Manager API"`
- All endpoints in `paths` section

---

## Verification Checklist

- [ ] Health check returns UP (Step 3)
- [ ] Unauthenticated `/me` returns 401 (Step 5)
- [ ] Google OAuth login works (Step 6)
- [ ] GitHub OAuth login works (Step 7)
- [ ] Session persists across refresh (Step 8)
- [ ] Logout clears session (Step 9)
- [ ] Multi-provider linking works (Step 10)
- [ ] Cross-user data isolation enforced (Step 11)
- [ ] All tests pass (Step 12)
- [ ] Build succeeds with no code quality issues (Step 13)
- [ ] Swagger UI loads with proper documentation (Step 14)

---

## Troubleshooting

### 400 Bad Request - redirect_uri_mismatch

**Cause**: OAuth redirect URI doesn't match registered URI in Google/GitHub

**Fix**:
1. Google: Settings → OAuth 2.0 Client ID → verify URI is exactly:
   ```
   http://localhost:8080/api/login/oauth2/code/google
   ```
2. GitHub: OAuth App settings → verify Authorization callback URL is exactly:
   ```
   http://localhost:8080/api/login/oauth2/code/github
   ```

### 401 Unauthorized on `/me` after browser login

**Cause**: Session cookie not sent with request

**Fix**:
- Use browser Swagger UI (automatically sends cookies)
- Or use curl with: `curl -b cookies.txt -c cookies.txt http://localhost:8080/api/auth/me`

### Tests fail with database connection error

**Cause**: MySQL not running or credentials wrong

**Fix**:
```bash
# Verify MySQL running
mysql -u taskuser -p -e "SELECT 1"

# Or update application.properties with correct values
```

### Checkstyle violations

**Cause**: Code formatting issues

**Fix**:
```bash
# View violations
./gradlew checkstyleMain

# Most violations auto-fixable in IDE
# Or reformat with: ./gradlew spotlessApply (if spotless plugin added)
```

---

## Next Steps

After verification passes all checks:
1. Proceed to **Phase 3: Lists Feature** implementation
2. Use same verification patterns for Phase 3 endpoints
3. Update BACKEND_PLAN.md with Phase 2 completion status

---

**Last Updated**: 2026-05-18
**Phase**: 2 (Authentication)
**Status**: Ready for verification
