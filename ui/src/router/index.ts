import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAuth } from '@/composables/useAuth';

const routes: RouteRecordRaw[] = [
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('@/pages/AuthPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/AuthPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/HomePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/home',
    name: 'HomeAlias',
    component: () => import('@/pages/HomePage.vue'),
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

let initialCheckDone = false;

router.beforeEach(async (to, from) => {
  const authStore = useAuthStore();
  const { fetchCurrentUser } = useAuth();

  // On first navigation, fetch current user to validate session
  if (!initialCheckDone) {
    try {
      await fetchCurrentUser();
    } catch (error) {
      // Session invalid or error - user stays logged out
    }
    initialCheckDone = true;
  }

  const requiresAuth = to.meta.requiresAuth as boolean;
  const isAuthPage = to.path === '/auth' || to.path === '/login';

  // If auth page and already logged in, go to home
  if (isAuthPage && authStore.isAuthenticated) {
    return '/';
  }
  // If page requires auth but not logged in, go to auth
  if (requiresAuth && !authStore.isAuthenticated) {
    return '/auth';
  }
});

export default router;
