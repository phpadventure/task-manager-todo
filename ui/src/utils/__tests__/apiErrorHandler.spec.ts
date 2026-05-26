import { describe, it, expect } from 'vitest';
import { AxiosError } from 'axios';
import {
  getErrorMessage,
  getStatusCodeMessage,
  isConflictError,
  isValidationError,
  isNotFoundError,
  isAuthError,
  extractFieldErrors,
} from '@/utils/apiErrorHandler';

describe('apiErrorHandler', () => {
  describe('getErrorMessage', () => {
    it('extracts backend error message from response', () => {
      const error = new AxiosError('Request failed', 'ERR_RESPONSE');
      error.response = {
        status: 400,
        data: {
          error: 'Validation failed',
          details: {},
        },
        statusText: 'Bad Request',
        headers: {},
        config: {} as any,
      };

      const result = getErrorMessage(error);
      expect(result.message).toBe('Validation failed');
      expect(result.statusCode).toBe(400);
    });

    it('extracts details.message as fallback', () => {
      const error = new AxiosError('Request failed');
      error.response = {
        status: 409,
        data: {
          details: {
            message: 'Cannot delete list with active tasks',
          },
        },
        statusText: 'Conflict',
        headers: {},
        config: {} as any,
      };

      const result = getErrorMessage(error);
      expect(result.message).toBe('Cannot delete list with active tasks');
      expect(result.statusCode).toBe(409);
    });

    it('uses status code message as fallback', () => {
      const error = new AxiosError('Request failed');
      error.response = {
        status: 404,
        data: {},
        statusText: 'Not Found',
        headers: {},
        config: {} as any,
      };

      const result = getErrorMessage(error);
      expect(result.statusCode).toBe(404);
      expect(result.message).toContain('not found');
    });

    it('handles regular Error objects', () => {
      const error = new Error('Something went wrong');
      const result = getErrorMessage(error);
      expect(result.message).toBe('Something went wrong');
    });

    it('handles unknown error types', () => {
      const result = getErrorMessage('string error');
      expect(result.message).toBe('An unexpected error occurred');
    });
  });

  describe('getStatusCodeMessage', () => {
    it('returns 400 validation message', () => {
      const msg = getStatusCodeMessage(400);
      expect(msg).toContain('Validation failed');
    });

    it('returns 401 auth message', () => {
      const msg = getStatusCodeMessage(401);
      expect(msg).toContain('session has expired');
    });

    it('returns 403 permission message', () => {
      const msg = getStatusCodeMessage(403);
      expect(msg).toContain('permission');
    });

    it('returns 404 not found message', () => {
      const msg = getStatusCodeMessage(404);
      expect(msg).toContain('not found');
    });

    it('returns 409 conflict message', () => {
      const msg = getStatusCodeMessage(409);
      expect(msg).toContain('conflict');
    });

    it('returns 500 server error message', () => {
      const msg = getStatusCodeMessage(500);
      expect(msg).toContain('Server error');
    });

    it('uses context in messages', () => {
      const msgWithContext = getStatusCodeMessage(404, 'task');
      expect(msgWithContext).toContain('task');

      const msgWithListContext = getStatusCodeMessage(404, 'list');
      expect(msgWithListContext).toContain('list');
    });

    it('returns generic message for unknown status codes', () => {
      const msg = getStatusCodeMessage(999);
      expect(msg).toContain('operation failed');
    });
  });

  describe('error type predicates', () => {
    const createAxiosError = (status: number): AxiosError => {
      const error = new AxiosError('Test error');
      error.response = {
        status,
        data: {},
        statusText: '',
        headers: {},
        config: {} as any,
      };
      return error;
    };

    it('isConflictError identifies 409 errors', () => {
      expect(isConflictError(createAxiosError(409))).toBe(true);
      expect(isConflictError(createAxiosError(400))).toBe(false);
    });

    it('isValidationError identifies 400 errors', () => {
      expect(isValidationError(createAxiosError(400))).toBe(true);
      expect(isValidationError(createAxiosError(500))).toBe(false);
    });

    it('isNotFoundError identifies 404 errors', () => {
      expect(isNotFoundError(createAxiosError(404))).toBe(true);
      expect(isNotFoundError(createAxiosError(400))).toBe(false);
    });

    it('isAuthError identifies 401 and 403 errors', () => {
      expect(isAuthError(createAxiosError(401))).toBe(true);
      expect(isAuthError(createAxiosError(403))).toBe(true);
      expect(isAuthError(createAxiosError(400))).toBe(false);
    });

    it('error predicates return false for non-AxiosError', () => {
      const error = new Error('Regular error');
      expect(isConflictError(error)).toBe(false);
      expect(isValidationError(error)).toBe(false);
      expect(isNotFoundError(error)).toBe(false);
      expect(isAuthError(error)).toBe(false);
    });
  });

  describe('extractFieldErrors', () => {
    it('extracts field errors from details object', () => {
      const details = {
        title: 'Title is required',
        dueDate: 'Due date must be in the future',
      };

      const result = extractFieldErrors(details);
      expect(result).toEqual({
        title: 'Title is required',
        dueDate: 'Due date must be in the future',
      });
    });

    it('ignores message field when extracting', () => {
      const details = {
        message: 'Validation failed',
        title: 'Title is required',
      };

      const result = extractFieldErrors(details);
      expect(result).toEqual({
        title: 'Title is required',
      });
    });

    it('returns null if no field errors found', () => {
      const details = {
        message: 'Some error',
      };

      const result = extractFieldErrors(details);
      expect(result).toBeNull();
    });

    it('handles null or undefined input', () => {
      expect(extractFieldErrors(null)).toBeNull();
      expect(extractFieldErrors(undefined)).toBeNull();
    });

    it('handles non-object input', () => {
      expect(extractFieldErrors('string')).toBeNull();
      expect(extractFieldErrors(123)).toBeNull();
    });

    it('includes field error in getErrorMessage', () => {
      const error = new AxiosError('Request failed');
      error.response = {
        status: 400,
        data: {
          error: 'Validation failed',
          details: {
            dueDate: 'Due date is required',
            title: 'Title must not be empty',
          },
        },
        statusText: 'Bad Request',
        headers: {},
        config: {} as any,
      };

      const result = getErrorMessage(error);
      expect(result.fieldErrors).toEqual({
        dueDate: 'Due date is required',
        title: 'Title must not be empty',
      });
    });
  });
});
