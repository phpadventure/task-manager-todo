import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

interface User {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null);
  const isAuthenticated = computed(() => user.value !== null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const currentUser = computed(() => user.value);

  const setUser = (userData: User | null) => {
    user.value = userData;
    error.value = null;
  };

  const setLoading = (state: boolean) => {
    loading.value = state;
  };

  const setError = (message: string | null) => {
    error.value = message;
  };

  const logout = () => {
    user.value = null;
    error.value = null;
  };

  return {
    user,
    isAuthenticated,
    loading,
    error,
    currentUser,
    setUser,
    setLoading,
    setError,
    logout,
  };
});
