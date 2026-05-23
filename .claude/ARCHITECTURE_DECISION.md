# Architecture Decision: Session-Based Auth Without Subdomains

**Date**: 2026-05-23  
**Decision**: Use simple localhost approach for local dev, Nginx for Docker

---

## Question Asked

> Why use subdomains like `api.localhost` and `app.localhost` when we can use different ports on the same localhost?

---

## Answer

### The Problem with Different Ports

**Browser Same-Origin Policy (SOP)**: Two URLs are only same-origin if they have identical:
1. Protocol (http, https)
2. Domain (localhost, example.com)
3. **Port** ← Different ports break same-origin!

```
localhost:5173  vs  localhost:8080
├─ Same protocol ✅
├─ Same domain ✅
└─ DIFFERENT ports ❌ → NOT same-origin
```

### Why This Matters for Cookies

Browsers **only send cookies automatically to same-origin requests**. If frontend and backend are on different ports, cookies won't be sent automatically:

```
Browser (localhost:5173)
  ├─ Page refresh
  ├─ Browser checks: "Is my current page same-origin as backend?"
  ├─ Backend is localhost:8080
  ├─ Answer: NO (different ports)
  └─ Result: Cookie NOT sent ❌
```

### The Old Solution (Subdomains)

Use subdomains + cookie domain rewrite:

```
app.localhost:5173  vs  api.localhost:8080
├─ Same protocol ✅
├─ Same parent domain (.localhost) ✅
├─ Different ports (but doesn't matter) ✅
└─ SAME-ORIGIN ✅ → Cookies sent automatically
```

**Problem**: Requires `/etc/hosts` edits, confusing for new developers.

### Our Solution (Vite Proxy)

**Vite dev server is a proxy that sits between browser and backend**:

```
Browser (localhost:5173)
  ├─ Requests /api/auth/me
  ├─ Vite intercepts (same origin as browser)
  ├─ Vite forwards to backend (localhost:8080)
  ├─ Backend responds with Set-Cookie header
  ├─ **Vite FORWARDS the Set-Cookie to browser**
  ├─ Browser sees Set-Cookie from Vite (same origin!)
  ├─ Browser stores cookie
  └─ Result: Cookie sent on next request ✅
```

**Key insight**: Vite is **transparent proxy** that acts like the backend from browser's perspective.

---

## Why We Chose Vite Proxy

### Advantages

✅ **No manual setup**: No `/etc/hosts` edits, no subdomain confusion  
✅ **Simpler for new developers**: Just run 2 commands, it works  
✅ **Vite built-in feature**: Already configured in `vite.config.ts`  
✅ **Clear documentation**: Easy to explain how cookies work  
✅ **Same for local + Docker**: Both forward/proxy cookies, just differently  

### Disadvantages

❌ Not "true" same-origin (Vite is intermediary)  
❌ Requires understanding Vite proxy behavior  
❌ Vite only for development (but that's the point)  

---

## When Each Approach Makes Sense

### Subdomains (app.localhost + api.localhost)

**Use when**:
- You want true same-origin for local dev
- You're testing multi-tenant scenarios
- You need hosts file edits for other reasons
- You want to match Docker setup exactly

**Skip when**:
- You want simplicity (just use Vite proxy)
- You're new to the project

### Vite Proxy (localhost:5173 + localhost:8080)

**Use when** (our choice):
- Local development is your daily workflow
- You want zero setup friction
- You want to minimize confusion
- You're teaching new developers

**Skip when**:
- You absolutely need true same-origin testing

### Nginx Reverse Proxy (Docker)

**Use when**:
- Running full stack with Docker
- Testing production-like setup
- Running in CI/CD
- You need true single-origin

**Skip when**:
- Local development (Vite is faster)

---

## Architecture Decision Matrix

```
┌─────────────────┬─────────────────┬──────────────┬────────────────┐
│ Scenario        │ Local Dev       │ Docker       │ Production     │
├─────────────────┼─────────────────┼──────────────┼────────────────┤
│ Frontend URL    │ localhost:5173  │ localhost:80 │ example.com    │
│ Backend URL     │ localhost:8080  │ backend:8080 │ backend.app    │
│ Proxy Type      │ Vite proxy      │ Nginx proxy  │ Nginx proxy    │
│ Same-Origin     │ Via Vite        │ Via Nginx    │ Via Nginx      │
│ Cookie Domain   │ localhost       │ localhost    │ example.com    │
│ Setup Needed    │ None            │ Docker       │ Infrastructure │
│ Hot Reload      │ ✅ Yes          │ ❌ No        │ ❌ N/A         │
│ Production-Like │ ❌ No           │ ✅ Yes       │ ✅ Yes         │
└─────────────────┴─────────────────┴──────────────┴────────────────┘
```

---

## What We Changed

### Files Updated

1. **README.md**
   - Removed `/etc/hosts` subdomain setup
   - Changed to simple `localhost:5173` + `localhost:8080`
   - Added explanation of Vite proxy

2. **BACKEND_PLAN.MD**
   - Removed subdomain setup from Phase 2
   - Simplified to: "Vite proxy forwards cookies"
   - Kept Docker section (Nginx reverse proxy)

3. **UI_PLAN.MD**
   - Already correct (using simple localhost)
   - No changes needed

4. **.claude/rules/reverse-proxy-setup.md**
   - Updated "Local Development" section
   - Removed subdomain references
   - Clarified Vite forwarding mechanism

5. **.claude/SESSION_COOKIES_EXPLANATION.md** (NEW)
   - Comprehensive guide on how cookies work
   - Local dev architecture explained
   - Docker architecture explained
   - Troubleshooting guide

---

## JSESSIONID Flow (How It Works End-to-End)

### Login

```
1. Browser (localhost:5173)
   └─ User clicks "Login with Google"
   └─ Button sends user to /oauth2/authorization/google

2. Request goes through Vite proxy → Backend (localhost:8080)

3. Backend
   ├─ Redirects to Google login page
   └─ User logs in with Google

4. Google redirects back to Backend
   ├─ Backend exchanges code for user info
   ├─ Backend creates session (stored server-side)
   └─ Backend sends response with Set-Cookie: JSESSIONID=xyz

5. Vite receives response from Backend
   └─ Vite forwards Set-Cookie header to Browser

6. Browser (localhost:5173)
   ├─ Sees Set-Cookie from Vite (same origin!)
   ├─ Stores cookie: JSESSIONID=xyz (domain=localhost)
   └─ Redirects to home page
```

### Page Refresh

```
1. Browser (localhost:5173) - User refreshes page

2. JavaScript runs on home page
   └─ Calls GET /api/auth/me (to check if logged in)

3. Vite proxy intercepts request
   ├─ Browser includes JSESSIONID cookie (same origin!)
   └─ Vite forwards request to Backend WITH cookie

4. Backend receives request with JSESSIONID
   ├─ Backend checks session store: "Session exists!"
   ├─ Backend: "User is authenticated"
   └─ Backend sends user profile in response

5. Browser receives response
   ├─ User data is set in store
   ├─ UI renders user profile
   └─ Result: User stays logged in ✅
```

---

## Testing the Setup

### Verify Cookies Work (Local Dev)

1. Start backend: `cd backend && ./gradlew bootRun`
2. Start frontend: `cd ui && npm run dev`
3. Open `http://localhost:5173`
4. Click "Login with Google/GitHub"
5. Complete OAuth flow
6. Open DevTools → Application → Cookies
7. Look for `JSESSIONID` cookie
8. Refresh page (Ctrl+R)
9. User should still be logged in ✅

### Verify Cookies Work (Docker)

1. `docker-compose up -d`
2. Open `http://localhost`
3. Complete OAuth flow
4. Open DevTools → Application → Cookies
5. Look for `JSESSIONID` cookie
6. Refresh page
7. User should still be logged in ✅

---

## Conclusion

**Decision**: Use Vite proxy for local dev (no subdomains), Nginx for Docker.

**Reasoning**:
- Simplest local dev experience (no manual setup)
- Clear documentation (see SESSION_COOKIES_EXPLANATION.md)
- Same cookie forwarding concept applies both locally and in Docker
- No confusion with subdomain prefixes

**Trade-off**: Local dev isn't "true" same-origin, but Vite proxy makes it work perfectly.

**Result**: All developers start with: `cd backend && ./gradlew bootRun` + `cd ui && npm run dev`, and it just works ✅
