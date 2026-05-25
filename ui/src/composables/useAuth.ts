import { computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { authApi } from '@/api/authApi';

export const useAuth = () => {
  const authStore = useAuthStore();

  const user = computed(() => authStore.currentUser);
  const isAuthenticated = computed(() => authStore.isAuthenticated);
  const loading = computed(() => authStore.loading);
  const error = computed(() => authStore.error);

  const fetchCurrentUser = async () => {
    authStore.setLoading(true);
    try {
      const userData = await authApi.getCurrentUser();
      authStore.setUser(userData);
    } catch (err: any) {
      const status = err?.response?.status;
      if (status !== 401) {
        authStore.setError(err instanceof Error ? err.message : 'Failed to fetch user');
      }
    } finally {
      authStore.setLoading(false);
    }
  };

  const loginWithGoogle = () => {
    authApi.loginWithGoogle();
  };

  const loginWithGitHub = () => {
    authApi.loginWithGitHub();
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      authStore.logout();
      window.location.href = '/auth';
    }
  };

  return {
    user,
    isAuthenticated,
    loading,
    error,
    fetchCurrentUser,
    loginWithGoogle,
    loginWithGitHub,
    logout,
  };
};
