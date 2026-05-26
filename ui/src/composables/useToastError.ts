import { useNotificationStore } from '@/stores/notificationStore';
import { getErrorMessage, getStatusCodeMessage } from '@/utils/apiErrorHandler';

export interface ErrorToastOptions {
  fallback?: string;
  duration?: number;
}

export function useToastError() {
  const notificationStore = useNotificationStore();

  const showError = (error: unknown, options?: ErrorToastOptions) => {
    const errorInfo = getErrorMessage(error);
    let message = errorInfo.message;

    // Use fallback only if no backend error message exists
    if (!message || message === 'An error occurred' || message === 'An unexpected error occurred') {
      message = options?.fallback || message || 'An error occurred';
    }

    notificationStore.addErrorNotification({
      message,
      duration: options?.duration,
    });
  };

  const showErrorWithContext = (
    error: unknown,
    context?: 'list' | 'task',
    options?: ErrorToastOptions
  ) => {
    const errorInfo = getErrorMessage(error);
    const contextLabel = context === 'list' ? 'list' : context === 'task' ? 'task' : 'operation';

    let message = errorInfo.message;

    // If we have a generic message or no message, use context-aware fallback
    if (
      !message ||
      message === 'An error occurred' ||
      message === 'An unexpected error occurred' ||
      message.includes('operation failed')
    ) {
      message =
        options?.fallback ||
        getStatusCodeMessage(errorInfo.statusCode, context) ||
        `${contextLabel} operation failed`;
    }

    notificationStore.addErrorNotification({
      message,
      duration: options?.duration,
    });
  };

  return {
    showError,
    showErrorWithContext,
  };
}
