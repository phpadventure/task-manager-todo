import { describe, it, expect, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '../authStore';

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('initializes with empty user', () => {
    const store = useAuthStore();
    expect(store.user).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  it('sets user correctly', () => {
    const store = useAuthStore();
    const testUser = {
      id: 'user-123',
      email: 'test@example.com',
      displayName: 'Test User',
      avatarUrl: 'https://example.com/avatar.jpg',
    };

    store.setUser(testUser);

    expect(store.user).toEqual(testUser);
    expect(store.isAuthenticated).toBe(true);
  });

  it('clears user on logout', () => {
    const store = useAuthStore();
    const testUser = {
      id: 'user-123',
      email: 'test@example.com',
      displayName: 'Test User',
      avatarUrl: 'https://example.com/avatar.jpg',
    };

    store.setUser(testUser);
    expect(store.isAuthenticated).toBe(true);

    store.logout();

    expect(store.user).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  it('returns current user via getter', () => {
    const store = useAuthStore();
    const testUser = {
      id: 'user-123',
      email: 'test@example.com',
      displayName: 'Test User',
      avatarUrl: 'https://example.com/avatar.jpg',
    };

    store.setUser(testUser);
    expect(store.currentUser).toEqual(testUser);
  });

  it('manages loading state', () => {
    const store = useAuthStore();
    expect(store.loading).toBe(false);

    store.setLoading(true);
    expect(store.loading).toBe(true);

    store.setLoading(false);
    expect(store.loading).toBe(false);
  });

  it('manages error state', () => {
    const store = useAuthStore();
    expect(store.error).toBeNull();

    store.setError('Auth failed');
    expect(store.error).toBe('Auth failed');

    store.setError(null);
    expect(store.error).toBeNull();
  });
});
