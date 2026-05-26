import { describe, it, expect, beforeEach } from 'vitest';
import { AxiosError } from 'axios';
import { useToastError } from '@/composables/useToastError';
import { useNotificationStore } from '@/stores/notificationStore';
import { createPinia, setActivePinia } from 'pinia';

describe('useToastError', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  describe('showError', () => {
    it('displays error toast with message from AxiosError', () => {
      const { showError } = useToastError();
      const store = useNotificationStore();
      const initialCount = store.notifications.length;

      const error = new AxiosError('Request failed');
      error.response = {
        status: 400,
        data: { error: 'Validation failed' },
        statusText: 'Bad Request',
        headers: {},
        config: {} as any,
      };

      showError(error);

      expect(store.notifications).toHaveLength(initialCount + 1);
      const lastNotif = store.notifications[store.notifications.length - 1];
      expect(lastNotif.type).toBe('error');
      expect((lastNotif as any).message).toBe('Validation failed');
    });

    it('uses backend error message over fallback', () => {
      const { showError } = useToastError();
      const store = useNotificationStore();

      const error = new AxiosError('Request failed');
      error.response = {
        status: 500,
        data: { error: 'Database connection error' },
        statusText: 'Server Error',
        headers: {},
        config: {} as any,
      };

      showError(error, { fallback: 'Operation failed' });

      const lastNotif = store.notifications[store.notifications.length - 1];
      // Backend error should be used, not fallback
      expect((lastNotif as any).message).toBe('Database connection error');
    });

    it('respects custom duration', () => {
      const { showError } = useToastError();
      const store = useNotificationStore();

      showError(new Error('Test'), { duration: 10000 });

      const lastNotif = store.notifications[store.notifications.length - 1];
      expect((lastNotif as any).duration).toBe(10000);
    });
  });

  describe('showErrorWithContext', () => {
    it('displays error with context-aware message for 404', () => {
      const { showErrorWithContext } = useToastError();
      const store = useNotificationStore();

      const error = new AxiosError('Request failed');
      error.response = {
        status: 404,
        data: {},
        statusText: 'Not Found',
        headers: {},
        config: {} as any,
      };

      showErrorWithContext(error, 'task');

      const lastNotif = store.notifications[store.notifications.length - 1];
      // Should contain 'task' in context-aware message
      expect((lastNotif as any).message).toMatch(/task|not found/i);
    });

    it('extracts backend error when available', () => {
      const { showErrorWithContext } = useToastError();
      const store = useNotificationStore();

      const error = new AxiosError('Request failed');
      error.response = {
        status: 409,
        data: { error: 'Cannot delete list with active tasks' },
        statusText: 'Conflict',
        headers: {},
        config: {} as any,
      };

      showErrorWithContext(error, 'list');

      const lastNotif = store.notifications[store.notifications.length - 1];
      expect((lastNotif as any).message).toBe('Cannot delete list with active tasks');
    });

    it('handles all context types', () => {
      const { showErrorWithContext } = useToastError();
      const store = useNotificationStore();

      const error = new Error('Test error');

      showErrorWithContext(error, 'list');
      let prevCount = store.notifications.length;

      showErrorWithContext(error, 'task');
      expect(store.notifications.length).toBeGreaterThan(prevCount);
      prevCount = store.notifications.length;

      showErrorWithContext(error);
      expect(store.notifications.length).toBeGreaterThan(prevCount);
    });
  });
});
