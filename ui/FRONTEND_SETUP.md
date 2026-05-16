# Frontend Setup Guide

## Project Structure

```
ui/
├── src/
│   ├── components/        # Vue components
│   ├── stores/           # Pinia state management
│   ├── pages/            # Page components
│   ├── services/         # API & WebSocket services
│   ├── App.vue
│   └── main.js
├── tests/                # E2E tests (Playwright)
├── playwright.config.ts  # Playwright configuration
├── vitest.config.ts      # Vitest configuration
├── tailwind.config.js    # Tailwind CSS config
├── postcss.config.js     # PostCSS config
├── vite.config.js        # Vite config
└── package.json
```

## Development Setup

### Prerequisites
- Node.js >= 20 (or use nvm: `nvm install 20` then `nvm use 20`)
- npm >= 8

### Install Dependencies
```bash
npm install
```

### Run Development Server
```bash
npm run dev
```
The app will be available at `http://localhost:5173`

### Build for Production
```bash
npm run build
```

## Testing

### Unit & Component Tests (Vitest)
```bash
# Run tests once
npm run test

# Run tests in watch mode
npm run test:watch

# Run a specific test file
npm run test src/components/TaskForm.spec.ts
```

### E2E Tests (Playwright)
```bash
# Run E2E tests (requires app running or auto-starts via webServer)
npm run test:e2e

# Run tests in interactive UI mode
npm run test:e2e:ui

# Run single test file
npx playwright test tests/auth.spec.ts

# View test report
npx playwright show-report
```

**Note**: On Node.js 16, Playwright browsers cannot install. Upgrade to Node 20+ or manually run: `npm install --legacy-peer-deps`

## Core Dependencies

| Package | Purpose |
|---------|---------|
| **vue** | Progressive framework |
| **@vitejs/plugin-vue** | Vue support in Vite |
| **tailwindcss** | Utility-first CSS framework |
| **@headlessui/vue** | Unstyled accessible components |
| **pinia** | State management |
| **@tanstack/vue-query** | Server state & data fetching |
| **@playwright/test** | E2E testing framework |
| **vitest** | Unit testing framework |

## Architecture

### State Management (Pinia)
Stores for:
- User authentication state
- Task list management
- UI state (modals, filters)

### Data Fetching (TanStack Query)
- Handles API requests with caching
- Automatic refetching & retries
- Server state synchronization

### WebSocket Service
- Real-time notifications for overdue/due-soon tasks
- Message formats defined in README
- Auto-reconnect on disconnect

### Components
- **Sidebar**: Project/list navigation
- **TaskList**: Display & filter tasks
- **TaskForm**: Create/edit tasks
- **TaskDetail**: View & manage single task
- **LoginPage**: OAuth login (Google & GitHub)

## OAuth Configuration

### Environment Variables (.env)
```
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=<your-client-id>
VITE_GITHUB_CLIENT_ID=<your-client-id>
```

See OAuth skill docs for complete setup.

## WebSocket Message Format

**Subscribe** (Client → Server):
```json
{ "type": "subscribe", "interval": 5000 }
```

**Notification** (Server → Client):
```json
{
  "type": "notification",
  "id": "notif-123",
  "taskId": "task-456",
  "title": "Task Title",
  "dueDate": "2026-05-17",
  "kind": "overdue" | "due-soon"
}
```

**Acknowledge** (Client → Server):
```json
{ "type": "ack", "notificationId": "notif-123" }
```

## Tailwind CSS Customization

Edit `tailwind.config.js` to customize colors, spacing, etc. Use Headless UI components for accessible form elements.

## Troubleshooting

### Vite Dev Server Not Starting
```bash
npm run dev
# If port 5173 is in use: npm run dev -- --port 3000
```

### Tests Fail with Node 16
Upgrade Node: `nvm install 20 && nvm use 20`

### Hot Module Replacement (HMR) Not Working
Clear cache: `rm -rf node_modules/.vite && npm run dev`

## Next Steps

1. Create OAuth login page (`src/pages/LoginPage.vue`)
2. Implement Pinia stores for auth & tasks
3. Set up API service layer
4. Build task management UI
5. Add WebSocket integration
6. Write integration tests with Playwright
