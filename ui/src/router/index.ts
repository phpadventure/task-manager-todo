import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

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
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.meta.requiresAuth as boolean;
  const isAuthPage = to.path === '/auth' || to.path === '/login';

  // If auth page and already logged in, go to home
  if (isAuthPage && authStore.isAuthenticated) {
    next('/');
  }
  // If page requires auth but not logged in, go to auth
  else if (requiresAuth && !authStore.isAuthenticated) {
    next('/auth');
  }
  // Otherwise, allow the navigation
  else {
    next();
  }
});

export default router;
