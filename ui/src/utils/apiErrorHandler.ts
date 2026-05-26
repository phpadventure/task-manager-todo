import { AxiosError } from 'axios';

export interface ApiErrorInfo {
  message: string;
  statusCode?: number;
  details?: unknown;
  fieldErrors?: Record<string, string>;
}

export function getErrorMessage(error: unknown): ApiErrorInfo {
  if (error instanceof AxiosError) {
    const statusCode = error.response?.status;
    const data = error.response?.data as any;

    // Try to extract backend error message
    if (data?.error) {
      const fieldErrors = extractFieldErrors(data.details);
      return {
        message: data.error,
        statusCode,
        details: data.details,
        fieldErrors: fieldErrors || undefined,
      };
    }

    // Try to extract details.message as fallback
    if (data?.details?.message) {
      const fieldErrors = extractFieldErrors(data.details);
      return {
        message: data.details.message,
        statusCode,
        details: data.details,
        fieldErrors: fieldErrors || undefined,
      };
    }

    // Fallback to status code messages
    const statusMessage = getStatusCodeMessage(statusCode);
    if (statusMessage) {
      return {
        message: statusMessage,
        statusCode,
      };
    }

    // Last resort: use error message or generic fallback
    return {
      message: error.message || 'An error occurred',
      statusCode,
    };
  }

  // Handle non-Axios errors
  if (error instanceof Error) {
    return { message: error.message };
  }

  return { message: 'An unexpected error occurred' };
}

export function extractFieldErrors(details: unknown): Record<string, string> | null {
  if (!details || typeof details !== 'object') {
    return null;
  }

  const detailsObj = details as Record<string, unknown>;
  const fieldErrors: Record<string, string> = {};
  let hasErrors = false;

  for (const [key, value] of Object.entries(detailsObj)) {
    if (key !== 'message' && typeof value === 'string') {
      fieldErrors[key] = value;
      hasErrors = true;
    }
  }

  return hasErrors ? fieldErrors : null;
}

export function getStatusCodeMessage(statusCode?: number, context?: 'list' | 'task'): string {
  const contextLabel = context === 'list' ? 'list' : context === 'task' ? 'task' : 'operation';

  switch (statusCode) {
    case 400:
      return `Validation failed. Please check your input and try again.`;
    case 401:
      return `Your session has expired. Please log in again.`;
    case 403:
      return `You don't have permission to perform this action.`;
    case 404:
      return `The ${contextLabel} was not found or has been deleted.`;
    case 409:
      return `A conflict occurred. The ${contextLabel} may have been modified by another action.`;
    case 500:
      return `Server error. Please try again later.`;
    case 503:
      return `Service unavailable. Please try again later.`;
    default:
      return `The ${contextLabel} operation failed. Please try again.`;
  }
}

export function isConflictError(error: unknown): boolean {
  return error instanceof AxiosError && error.response?.status === 409;
}

export function isValidationError(error: unknown): boolean {
  return error instanceof AxiosError && error.response?.status === 400;
}

export function isNotFoundError(error: unknown): boolean {
  return error instanceof AxiosError && error.response?.status === 404;
}

export function isAuthError(error: unknown): boolean {
  return (
    error instanceof AxiosError &&
    (error.response?.status === 401 || error.response?.status === 403)
  );
}
