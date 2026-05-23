# Session Cookies (JSESSIONID) - Local Dev vs Docker

**Last Updated**: 2026-05-23

---

## Quick Summary

| Environment | Frontend | Backend | How Cookies Work | Setup |
|-------------|----------|---------|------------------|-------|
| **Local Dev** | localhost:5173 | localhost:8080 | Vite proxy forwards cookies | Simple - no config |
| **Docker** | localhost (via Nginx) | localhost:8080 (internal) | Single origin (Nginx) | Docker Compose |

---

## Local Development: How It Works

### Architecture

```
┌──────────────────────────────────────────────────────────┐
│ User's Browser                                           │
│ Sees: http://localhost:5173 (frontend)                  │
└──────────────────────────────────────────────────────────┘
                      ↓ (user clicks login button)
┌──────────────────────────────────────────────────────────┐
│ Vite Dev Server (localhost:5173)                         │
│ ├─ Serves Vue app                                        │
│ ├─ Proxies /api/* to backend                             │
│ └─ Forwards Set-Cookie headers back to browser           │
└──────────────────────────────────────────────────────────┘
                      ↓ (redirected to OAuth)
┌──────────────────────────────────────────────────────────┐
│ Backend (localhost:8080)                                 │
│ ├─ Handles OAuth with Google/GitHub                      │
│ ├─ Creates session (stored in memory/database)           │
│ └─ Sends response with Set-Cookie: JSESSIONID=xyz        │
└──────────────────────────────────────────────────────────┘
                      ↓ (response flows back through Vite)
┌──────────────────────────────────────────────────────────┐
│ Browser Receives                                         │
│ ├─ Response from Vite (same origin as page)              │
│ ├─ Vite included Set-Cookie header                       │
│ └─ Browser stores JSESSIONID cookie                      │
└──────────────────────────────────────────────────────────┘
                      ↓ (user clicks a button in app)
┌──────────────────────────────────────────────────────────┐
│ Next Request to /api/auth/me                             │
│ ├─ Browser includes JSESSIONID cookie (automatically)    │
│ ├─ Vite proxy forwards cookie to backend                 │
│ ├─ Backend receives request WITH session cookie         │
│ └─ Backend: "Session exists, user is authenticated" ✅   │
└──────────────────────────────────────────────────────────┘
```

### Why This Works

**Vite Proxy Magic**:
1. Browser requests `/api/auth/me`
2. Vite proxy intercepts (same origin as browser)
3. Vite forwards to `http://localhost:8080/api/auth/me`
4. Backend responds with Set-Cookie header
5. **Vite includes Set-Cookie in response** ← This is the key!
6. Browser sees Set-Cookie from Vite (same origin)
7. Browser stores cookie normally
8. Next request: Browser includes cookie in request
9. Vite forwards cookie to backend
10. Backend receives authenticated request ✅

### Critical Config

**Vite proxy config** (ui/vite.config.ts):
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    credentials: 'include'  // ← This tells Vite to forward cookies
  }
}
```

**Axios config** (ui/src/api/client.ts):
```typescript
axios.create({
  withCredentials: true  // ← This tells axios to include cookies
})
```

### No Setup Required

✅ Just run:
```bash
# Terminal 1
cd backend && ./gradlew bootRun  # http://localhost:8080

# Terminal 2
cd ui && npm run dev  # http://localhost:5173
```

- No `/etc/hosts` edits
- No subdomain setup
- Vite handles everything automatically

---

## Docker: How It Works

### Architecture

```
┌──────────────────────────────────────────────────────────┐
│ User's Browser                                           │
│ Sees: http://localhost (Nginx on port 80)                │
└──────────────────────────────────────────────────────────┘
                      ↓ (single origin for everything)
┌──────────────────────────────────────────────────────────┐
│ Nginx Reverse Proxy (localhost:80)                       │
│ ├─ GET / → Vue static files (from backend)               │
│ ├─ GET /api/* → Backend :8080                            │
│ └─ GET /api/ws → Backend :8080 (WebSocket)               │
└──────────────────────────────────────────────────────────┘
                      ↓ (routes to backend via Docker network)
┌──────────────────────────────────────────────────────────┐
│ Backend (backend:8080 on Docker network)                 │
│ ├─ Handles all requests                                  │
│ ├─ Returns session cookie in response                    │
│ └─ Nginx forwards response back to browser               │
└──────────────────────────────────────────────────────────┘
                      ↓ (response from Nginx)
┌──────────────────────────────────────────────────────────┐
│ Browser Receives (via Nginx)                             │
│ ├─ Response from http://localhost (same origin!)         │
│ ├─ Set-Cookie: JSESSIONID=xyz                            │
│ └─ Browser stores cookie                                 │
└──────────────────────────────────────────────────────────┘
                      ↓ (next request)
┌──────────────────────────────────────────────────────────┐
│ Browser sends request with cookie                        │
│ ├─ Cookie scoped to http://localhost                     │
│ ├─ Sent to Nginx (same origin)                           │
│ ├─ Nginx forwards to backend                             │
│ └─ Backend receives authenticated request ✅             │
└──────────────────────────────────────────────────────────┘
```

### Why This Works

**True Same-Origin**:
- Everything goes through `http://localhost:80` (Nginx)
- Browser sees single origin for all requests
- Cookies set by backend are automatically sent to all requests
- No proxy magic needed

### Setup Required

**docker-compose.yml**:
- Frontend: Built Vue SPA (static files served by Nginx)
- Backend: Spring Boot on :8080
- Nginx: Reverse proxy on :80 (routes `/` to frontend, `/api/*` to backend)
- MySQL: Database

Run:
```bash
docker-compose up -d
# Access: http://localhost (no port needed, defaults to 80)
```

**Backend config** (application.properties):
```properties
server.servlet.session.cookie.domain=localhost
server.servlet.session.cookie.secure=true  # HTTPS in production
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=Lax
```

---

## Comparison

| Aspect | Local Dev | Docker |
|--------|-----------|--------|
| **Frontend URL** | http://localhost:5173 | http://localhost:80 |
| **Backend URL** | http://localhost:8080 | http://backend:8080 (internal) |
| **Cookie Domain** | localhost | localhost |
| **How Cookies Sent** | Vite proxy forwards | Single origin (Nginx) |
| **Manual Setup** | None | docker-compose.yml (pre-configured) |
| **Hosts File** | Not needed | Not needed |
| **Subdomains** | Not needed | Not needed |
| **Hot Reload** | ✅ Vite handles it | ❌ Requires rebuild |
| **Production-Like** | ❌ Dev-only Vite proxy | ✅ Realistic Nginx setup |

---

## Troubleshooting

### Local Dev Issues

**Problem**: Page refresh shows 401 (not authenticated)
```
Solution:
1. Check Vite proxy config has credentials: 'include'
2. Check axios config has withCredentials: true
3. Check DevTools → Network → verify Set-Cookie header present in response
4. Check DevTools → Application → Cookies → JSESSIONID present
```

**Problem**: JSESSIONID cookie not in DevTools
```
Solution:
1. Did you complete the OAuth login flow?
2. Check browser console for errors
3. Check backend logs for auth errors
4. Verify backend is running on localhost:8080
```

**Problem**: Vite shows CORS error
```
Solution:
1. Check vite.config.ts proxy config exists
2. Verify target is http://localhost:8080 (not 8080 without http://)
3. Restart Vite dev server after config changes
```

### Docker Issues

**Problem**: Session cookie not persisting
```
Solution:
1. Check docker-compose logs: docker-compose logs -f backend
2. Verify Nginx is routing /api/* to backend
3. Check backend is listening on port 8080
4. Check docker-compose.yml backend service config
```

**Problem**: Can't access http://localhost
```
Solution:
1. Verify Nginx container is running: docker-compose ps
2. Check Nginx config in docker/nginx/default.conf
3. Try accessing backend directly: http://localhost:8080/api/auth/me
4. Check if port 80 is in use: lsof -i :80
```

---

## Development Workflow

### Local Dev (Feature Work)

1. Run backend and frontend on localhost
2. Vite automatically proxies API calls
3. Hot reload works for both frontend and backend (on changes)
4. Easy debugging (no Docker overhead)

```bash
# Start backend
cd backend && ./gradlew bootRun

# Start frontend (in another terminal)
cd ui && npm run dev

# Open http://localhost:5173
```

### Docker (Integration Testing)

1. Build and run full stack (all services together)
2. Production-like setup (Nginx reverse proxy)
3. Can test multi-container interactions
4. More representative of production environment

```bash
# Start all services
docker-compose up -d

# Open http://localhost
```

---

## Key Takeaway

**Local Dev**: Vite proxy handles cookies automatically ✅  
**Docker**: Nginx reverse proxy handles cookies automatically ✅  
**Both**: No manual cookie handling needed - it just works!

---

## References

- **Backend Cookie Config**: `backend/src/main/resources/application.properties`
- **Vite Proxy Config**: `ui/vite.config.ts`
- **Axios Config**: `ui/src/api/client.ts`
- **Docker Setup**: `docker-compose.yml` + `docker/nginx/default.conf`
- **Spring Security Config**: `backend/src/main/java/com/taskmanager/config/SecurityConfig.java`
