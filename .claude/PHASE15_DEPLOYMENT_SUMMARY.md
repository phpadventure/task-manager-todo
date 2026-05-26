# Phase 15: Docker Deployment Implementation Summary

**Date**: 2026-05-26  
**Status**: ✅ COMPLETE  
**Scope**: Multi-stage Docker build, Nginx reverse proxy, single-origin deployment

---

## Implementation Overview

Completed Phase 15 deployment infrastructure with:
- **Frontend Dockerfile** with multi-stage build (Node → Nginx)
- **Nginx reverse proxy** with single-origin routing
- **Docker Compose** orchestration with 4 services
- **Environment variables** for CORS configuration
- **README documentation** for Docker operations

---

## Files Created

### 1. **ui/Dockerfile** (Multi-Stage Build)
```dockerfile
Stage 1 (Build): node:20-alpine
  - npm ci (install dependencies)
  - npm run build (Vue + Vite)
  - Output: /app/dist (static SPA files)

Stage 2 (Runtime): nginx:latest
  - Copy dist from build stage
  - Copy docker/nginx/default.conf
  - Expose port 80
  - Run: nginx -g "daemon off;"
```

**Key Features**:
- ✅ Minimal image size (only nginx + dist, no source code)
- ✅ Production-ready (alpine images)
- ✅ Asset caching configured
- ✅ Zero-downtime deployment

### 2. **docker/nginx/default.conf** (Reverse Proxy)
```nginx
Upstream: backend:8080 (internal Docker service)

Routing Rules:
  /          → /usr/share/nginx/html/index.html (SPA fallback)
  /assets/*  → /usr/share/nginx/html/assets (30-day cache)
  /api/*     → http://backend:8080/api (reverse proxy)
  /api/ws    → ws://backend:8080/api/ws (WebSocket upgrade)
```

**Key Features**:
- ✅ Single-origin setup (localhost:80)
- ✅ WebSocket support (Upgrade headers)
- ✅ Cookie forwarding (session persistence)
- ✅ Gzip compression enabled
- ✅ Static asset caching (30 days)

---

## Files Updated

### 3. **docker-compose.yml** (Service Orchestration)

**New Services Added**:

| Service | Image/Build | Port | Network | Purpose |
|---------|-------------|------|---------|---------|
| mysql | mysql:8.0 | :3306 | task-manager-network | Database |
| backend | ./backend/Dockerfile | :8080 | task-manager-network | API |
| ui | ./ui/Dockerfile | :80 | task-manager-network | Frontend build |
| nginx | nginx:latest | 80:80 | task-manager-network | Reverse proxy |

**Key Changes**:
- ✅ Added explicit `task-manager-network` (bridge)
- ✅ Backend: `expose: [8080]` + `ports: [8080:8080]` (debug access)
- ✅ UI: `expose: [80]` (only internal)
- ✅ Nginx: `ports: [80:80]` (public access)
- ✅ All services depend on mysql health check
- ✅ Backend depends on mysql; nginx depends on backend + ui
- ✅ Environment variable: `APP_CORS_ALLOWED_ORIGINS` passed to backend

**Service Dependencies** (startup order):
```
mysql (startup first, wait for health check)
  ↓ (mysql healthy)
backend (startup)
  ↓ (backend ready)
ui (startup in parallel)
nginx (startup after backend + ui)
  ↓
System ready for requests on localhost:80
```

### 4. **Environment Variables** (CORS Configuration)

**Updated Files**:
- ✅ `.env.example` - Added APP_CORS_ALLOWED_ORIGINS
- ✅ `.env` - Added APP_CORS_ALLOWED_ORIGINS
- ✅ `backend/.env.example` - Added APP_CORS_ALLOWED_ORIGINS

**Default Value**:
```
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost
```

**Usage**:
- Local dev: `http://localhost:5173` (Vite proxy)
- Alt dev: `http://localhost:3000` (alternative port)
- Docker: `http://localhost` (Nginx single-origin)

### 5. **application.properties** (Backend Config)

**Before**:
```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

**After**:
```properties
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000,http://localhost}
```

**Benefits**:
- ✅ Externalized config (works with env variables)
- ✅ Fallback to sensible defaults
- ✅ Docker-friendly (no code changes needed)

### 6. **README.md** (Documentation)

**Sections Updated**:
- ✅ Docker access instructions (http://localhost)
- ✅ Architecture explanation (Nginx + services)
- ✅ Service status verification (`docker-compose ps`)
- ✅ Logging instructions (all 4 services)
- ✅ Rebuild commands (backend, ui, both, all)
- ✅ Optional Swagger UI access (direct backend)

---

## Architecture Comparison

| Aspect | Local Dev | Docker |
|--------|-----------|--------|
| **Frontend URL** | localhost:5173 | localhost:80 |
| **Backend URL** | localhost:8080 | localhost:80 (via Nginx) |
| **Origin** | Two ports (Vite proxy) | Single origin (Nginx) |
| **Proxy Type** | Vite dev proxy | Nginx reverse proxy |
| **Services** | 1 (Vite) | 4 (nginx, ui, backend, mysql) |
| **Session Cookies** | Forwarded by Vite | Forwarded by Nginx |
| **CORS** | Backend allows localhost:5173 | Same-origin (no CORS needed) |
| **Database** | Local MySQL or Docker | Docker MySQL |
| **Hot Reload** | ✅ Vite dev server | ❌ Rebuild required |
| **Production-Like** | ❌ Dev-only | ✅ Yes |

---

## Network Architecture (Docker)

```
┌─────────────────────────────────────────────────────────────┐
│ Host Machine                                                │
│ ┌───────────────────────────────────────────────────────┐  │
│ │ Browser at http://localhost                          │  │
│ └─────────────────────┬─────────────────────────────────┘  │
│                       │                                      │
│                       ↓ (port 80)                           │
│ ┌─────────────────────────────────────────────────────────┐  │
│ │ Docker Compose Bridge Network (task-manager-network) │  │
│ │                                                        │  │
│ │  ┌──────────────┐                                     │  │
│ │  │ Nginx:80     │ (public port exposed)               │  │
│ │  │              │                                     │  │
│ │  └──────┬───────┘                                     │  │
│ │         │ (internal routes)                           │  │
│ │    ┌────┴─────┬──────────┐                            │  │
│ │    │           │          │                           │  │
│ │    ↓           ↓          ↓                           │  │
│ │ ┌──────┐  ┌─────────┐  ┌──────────┐                 │  │
│ │ │ UI   │  │ Backend │  │ MySQL    │                 │  │
│ │ │:80   │  │ :8080   │  │ :3306    │                 │  │
│ │ └──────┘  └────┬────┘  └──────────┘                 │  │
│ │                │                                     │  │
│ │                └──── (queries database)             │  │
│ └────────────────────────────────────────────────────────┘  │
│ (optional: backend port 8080 exposed to host for debugging)  │
└─────────────────────────────────────────────────────────────┘
```

---

## How It Works: Session Cookies in Docker

### Login Flow
```
1. User opens http://localhost (browser)
2. Nginx routes / to UI (frontend)
3. User clicks "Login with Google/GitHub"
4. Browser navigates to /api/oauth2/authorization/google
5. Nginx proxies to backend:8080
6. Backend redirects to Google OAuth page
7. User logs in with Google
8. Google redirects to backend with auth code
9. Backend creates session + sends Set-Cookie: JSESSIONID
10. Nginx forwards Set-Cookie to browser
11. Browser stores cookie (domain: localhost)
```

### Subsequent Requests
```
1. User clicks task list item
2. Frontend calls GET /api/lists
3. Browser includes JSESSIONID cookie (same-origin)
4. Nginx forwards request + cookie to backend
5. Backend receives authenticated request
6. Backend returns data
→ User session persists ✅
```

---

## Verification Steps

### 1. **Start Services**
```bash
docker-compose up -d
# Wait 30-40 seconds for MySQL health check + startup
```

### 2. **Verify All Services Running**
```bash
docker-compose ps
# All services should show "Up"
```

### 3. **Access Frontend**
```bash
# Browser or curl
curl http://localhost
# Should return index.html (Vue app)
```

### 4. **Test API Through Proxy**
```bash
curl http://localhost/api/actuator/health
# Should return: {"status":"UP"}
```

### 5. **Verify Session Cookie**
```bash
# Login via OAuth, then:
curl -v http://localhost/api/auth/me
# Response should include Set-Cookie: JSESSIONID
# On refresh, cookie should be sent automatically
```

### 6. **Test WebSocket**
```bash
# After login, create a task
# WebSocket should connect at /api/ws
# Should receive overdue/due-soon notifications
```

### 7. **View Logs**
```bash
docker-compose logs -f nginx     # Routing logs
docker-compose logs -f backend   # API logs
docker-compose logs -f ui        # Frontend build logs
docker-compose logs -f mysql     # Database logs
```

---

## Local Development Verification

**Still Works**:
- ✅ `npm run dev` (localhost:5173 with Vite proxy)
- ✅ `./gradlew bootRun` (localhost:8080 backend directly)
- ✅ Separate MySQL (docker or local)
- ✅ OAuth login through Vite proxy
- ✅ Hot reload for Vue components
- ✅ Tests (Vitest, Playwright)

**No Changes Needed**:
- Vite config already correct (proxy, credentials)
- Axios config already correct (baseURL, withCredentials)
- Backend CORS already configured

---

## Docker Rebuild Scenarios

### **After Frontend Code Change**
```bash
docker-compose up -d --build ui
# Rebuilds ui service (npm run build + Nginx)
```

### **After Backend Code Change**
```bash
docker-compose up -d --build backend
# Rebuilds backend service
```

### **After Nginx Config Change**
```bash
docker-compose restart nginx
# Restarts nginx with updated config
```

### **Full Rebuild** (all services)
```bash
docker-compose up -d --build
# Rebuilds all services
```

### **Clean Slate** (remove everything)
```bash
docker-compose down -v
# Removes containers, volumes, networks
# MySQL data deleted (mysql_data volume removed)
```

---

## Production Considerations

**Already Included** ✅:
- Multi-stage Docker builds (smaller images)
- Nginx reverse proxy (single origin)
- Health checks (MySQL)
- Explicit networking (task-manager-network)
- Environment variable externalization
- Session cookie configuration (HttpOnly, SameSite=Lax)

**Recommended Additions** (not in scope):
- HTTPS/TLS (use Let's Encrypt for production)
- Environment-specific configs (dev.env, prod.env)
- Container orchestration (Kubernetes)
- CI/CD pipeline (GitHub Actions)
- Load balancing (if needed)
- Monitoring (Prometheus, Grafana)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Nginx returns 502 Bad Gateway | Backend not ready, wait for startup: `docker-compose logs backend` |
| Session not persisting | Verify nginx routes /api to backend, cookie domain is localhost |
| Frontend shows 404 | Check nginx config routes / to ui service (not backend) |
| WebSocket not connecting | Verify nginx forwards Upgrade headers for /api/ws |
| Port 80 already in use | Change docker-compose.yml: `"8080:80"` instead of `"80:80"` |
| MySQL connection failed | Wait for health check: `docker-compose logs mysql` |
| Frontend build failed | Check ui/Dockerfile, verify `npm run build` works locally |

---

## Summary of Changes

### New Files (2)
- ✅ `ui/Dockerfile`
- ✅ `docker/nginx/default.conf`

### Modified Files (6)
- ✅ `docker-compose.yml`
- ✅ `.env.example`
- ✅ `.env`
- ✅ `backend/.env.example`
- ✅ `backend/src/main/resources/application.properties`
- ✅ `README.md`

### Updated Documentation (1)
- ✅ `UI_PLAN.MD` (Phase 15 marked complete)

---

## Next Steps (Phase 16+)

- Optional: Add CI/CD pipeline (GitHub Actions)
- Optional: Add component documentation (COMPONENT_GUIDE.md)
- Optional: Bundle size analysis and optimization
- Ready for production deployment with Docker!

---

## References

- **Reverse Proxy Strategy**: `.claude/rules/reverse-proxy-setup.md`
- **Session Cookies**: `.claude/SESSION_COOKIES_EXPLANATION.md`
- **Architecture Decision**: `.claude/ARCHITECTURE_DECISION.md`
- **Backend CLAUDE.md**: `backend/CLAUDE.md` (build commands)
- **Frontend Setup**: `ui/README.md` (Vite configuration)

---
