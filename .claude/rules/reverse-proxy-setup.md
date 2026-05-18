---
name: Reverse Proxy Pattern for Session Auth (Docker Only)
description: Nginx reverse proxy required for production Docker deployment to serve frontend and backend through single origin
type: reference
---

# Reverse Proxy Pattern for Session Auth (Docker Only)

## Rule

Session-based cookies require same-origin. In **production Docker**, Nginx reverse proxy serves both frontend and backend through single origin (`http://localhost`).

**Pattern**: `Nginx:80` → routes `/` to frontend, `/api` to backend, `/api/ws` to WebSocket.

## Local Development (No Nginx)

Local dev uses **Vite proxy + hosts file** (see UI_PLAN.MD):
- Edit `/etc/hosts`: `127.0.0.1 app.localhost api.localhost`
- Frontend: `http://app.localhost:5173`
- Backend: `http://api.localhost:8080`
- Vite config: proxy `/api` to `http://api.localhost:8080` with `cookieDomainRewrite: '.localhost'`

## Docker Deployment

Three services: Nginx, Backend, MySQL.

**Nginx routes**:
- `GET /` → static Vue files
- `GET /api/*` → backend HTTP
- `GET /api/ws` → backend WebSocket

**Backend**:
- Runs on `backend:8080` (exposed only to Nginx, not to host)
- Session cookie domain: `localhost`

**Cookie config** (Spring Security - uses configurable domain):
```java
String cookieDomain = environment.getProperty("server.servlet.session.cookie.domain", "localhost");
config.setDomain(cookieDomain);
config.setHttpOnly(true);
config.setSameSite("Lax");
```

**application.properties** (configurable per environment):
```properties
# Local dev (Vite proxy + hosts file)
server.servlet.session.cookie.domain=.localhost

# Docker (single origin via Nginx)
server.servlet.session.cookie.domain=localhost

# Production (real domain)
server.servlet.session.cookie.domain=example.com
```

See `docker/nginx/default.conf` for implementation details (created in Phase 6).
