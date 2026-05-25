# Reverse Proxy Pattern for Session Auth (Docker Only)

## Rule

Session-based cookies require same-origin. In **production Docker**, Nginx reverse proxy serves both frontend and backend through single origin (`http://localhost`).

**Pattern**: `Nginx:80` → routes `/` to frontend, `/api` to backend, `/api/ws` to WebSocket.

## Local Development (Vite Proxy + Backend CORS)

Local dev uses **Vite proxy + Backend CORS** to forward requests and cookies:
- Frontend: `http://localhost:5173` (Vite dev server)
- Backend: `http://localhost:8080` (Spring Boot)
- Vite config: proxy `/api` to `http://localhost:8080` with `credentials: 'include'` and `changeOrigin: true`
- Axios baseURL: `/api` (relative, so Vite intercepts)
- Backend CORS: Allow origin `http://localhost:5173` with `allowCredentials: true`
- **No hosts file changes needed**
- **No Nginx needed**

**How it works**: 
1. Vite proxy intercepts `/api` requests from browser (same origin from browser's perspective)
2. Vite forwards to backend with `credentials: 'include'`
3. Backend CORS validates origin `localhost:5173` is allowed
4. Backend sends Set-Cookie + CORS headers
5. Vite forwards response to browser (as same-origin)
6. Browser stores cookie (CORS headers validate cross-origin)
7. Future requests automatically include cookie ✅

**Why CORS is needed**: Even with Vite proxy, browser enforces CORS policy. Explicit backend CORS config is necessary for:
- Handling CORS preflight (OPTIONS) requests
- Validating credentials are allowed with `Access-Control-Allow-Credentials: true`
- Explicit permission from backend for cross-origin requests

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
# Local dev (Vite proxy)
server.servlet.session.cookie.domain=localhost

# Docker (single origin via Nginx)
server.servlet.session.cookie.domain=localhost

# Production (real domain)
server.servlet.session.cookie.domain=example.com
```

See `docker/nginx/default.conf` for implementation details (created in Phase 6).
