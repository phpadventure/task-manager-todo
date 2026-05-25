# Session Cookies (JSESSIONID) - Local Dev vs Docker

**Last Updated**: 2026-05-23

---

## Quick Summary

| Environment | Frontend | Backend | How Cookies Work | Setup |
|-------------|----------|---------|------------------|-------|
| **Local Dev** | localhost:5173 | localhost:8080 | Vite proxy forwards cookies | Simple - no config |
| **Docker** | localhost (via Nginx) | localhost:8080 (internal) | Single origin (Nginx) | Docker Compose |

---

## Local Development: How It Works (With CORS)

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
│ └─ Backend CORS allows localhost:5173                    │
└──────────────────────────────────────────────────────────┘
                      ↓ (redirected to OAuth)
┌──────────────────────────────────────────────────────────┐
│ Backend (localhost:8080)                                 │
│ ├─ Handles OAuth with Google/GitHub                      │
│ ├─ Creates session (stored in memory/database)           │
│ ├─ Sends response with Set-Cookie: JSESSIONID=xyz        │
│ └─ Sends CORS headers: Allow-Origin=localhost:5173 ✅    │
└──────────────────────────────────────────────────────────┘
                      ↓ (response flows back through Vite)
┌──────────────────────────────────────────────────────────┐
│ Browser Receives                                         │
│ ├─ Response from Vite (same origin as page)              │
│ ├─ Vite included Set-Cookie header                       │
│ ├─ Browser sees CORS headers (validates cross-origin)    │
│ └─ Browser stores JSESSIONID cookie                      │
└──────────────────────────────────────────────────────────┘
                      ↓ (user clicks a button in app)
┌──────────────────────────────────────────────────────────┐
│ Next Request to /api/auth/me                             │
│ ├─ Browser includes JSESSIONID cookie (automatically)    │
│ ├─ Vite proxy forwards cookie to backend                 │
│ ├─ Backend receives request WITH session cookie         │
│ ├─ Backend CORS check passes (cookie allowed)           │
│ └─ Backend: "Session exists, user is authenticated" ✅   │
└──────────────────────────────────────────────────────────┘
```

### Why This Works (Now With CORS)

**Vite Proxy + Backend CORS**:
1. Browser requests `/api/auth/me` (via Vite proxy)
2. Vite proxy intercepts (same origin as browser)
3. Vite forwards to `http://localhost:8080/api/auth/me` with `credentials: include`
4. Backend receives request, checks CORS (localhost:5173 is allowed ✅)
5. Backend responds with Set-Cookie header + CORS headers
6. **Vite includes Set-Cookie in response** ← Key!
7. Browser sees Set-Cookie from Vite (same origin)
8. Browser stores cookie normally (CORS headers validate)
9. Next request: Browser includes cookie in request
10. Vite forwards cookie to backend (with credentials)
11. Backend receives authenticated request ✅

### Critical Config

**Vite proxy config** (ui/vite.config.ts):
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    credentials: 'include'  // ← Tells Vite to forward cookies
  }
}
```

**Axios config** (ui/src/api/client.ts):
```typescript
axios.create({
  baseURL: '/api',              // ← Relative URL so Vite proxy intercepts
  withCredentials: true         // ← Tells axios to include cookies
})
```

**Backend CORS config** (backend/src/main/java/com/taskmanager/config/SecurityConfig.java):
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration configuration = new CorsConfiguration();
  configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
  configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
  configuration.setAllowedHeaders(Arrays.asList("*"));
  configuration.setAllowCredentials(true);  // ← CRITICAL: Allow credentials with CORS
  configuration.setMaxAge(3600L);
  
  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/**", configuration);
  return source;
}
```

### Setup Required

✅ Just run:
```bash
# Terminal 1
cd backend && ./gradlew bootRun  # http://localhost:8080

# Terminal 2
cd ui && npm run dev  # http://localhost:5173
```

**Configuration already in place**:
- ✅ Vite proxy configured with `credentials: 'include'`
- ✅ Axios baseURL set to `/api` (relative, so proxy intercepts)
- ✅ Backend CORS enabled via SecurityConfig
- ✅ No `/etc/hosts` edits needed
- ✅ No subdomain setup needed

---

## Why CORS Was Necessary (Lessons Learned)

### Initial Attempt: Vite Proxy Alone

**Theory**: Vite proxy would fully handle cookies, so we didn't need CORS on the backend.

**Problem**: Despite Vite proxying requests, browsers still enforced CORS policy:
```
Error: Access to XMLHttpRequest at 'http://localhost:8080/api/auth/me' 
from origin 'http://localhost:5173' has been blocked by CORS policy: 
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

### Why CORS Was Still Needed

Even with Vite proxy, the browser's Same-Origin Policy still applies in several scenarios:

1. **Absolute vs Relative URLs**: 
   - If axios uses `baseURL: 'http://localhost:8080/api'` (absolute), it bypasses Vite proxy entirely
   - Browser makes direct request to port 8080 → CORS error
   - **Fix**: Use `baseURL: '/api'` (relative) so Vite proxy intercepts

2. **Browser Pre-flight Requests**:
   - Complex requests (with credentials, certain headers) trigger CORS preflight (OPTIONS)
   - Browser makes OPTIONS request to check if cross-origin is allowed
   - Vite proxy might not handle preflight perfectly
   - **Fix**: Backend CORS config responds to OPTIONS with allow headers

3. **Defense in Depth**:
   - CORS acts as explicit permission from backend that cross-origin requests are OK
   - Cookies with `credentials: true` require explicit `Access-Control-Allow-Credentials: true`
   - **Fix**: Backend CORS sets `allowCredentials(true)`

### The Complete Solution

**Three-layer approach**:
1. **Vite proxy** → Forwards requests server-side (hides cross-origin from first layer)
2. **Backend CORS** → Explicitly allows localhost:5173 + credentials
3. **Axios/Vite credentials** → Both configured to include cookies

**Result**: Vite proxy handles most requests transparently, CORS acts as security layer for edge cases + credentials.

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
