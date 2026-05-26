import { describe, it, expect } from 'vitest';
import {
  validateTaskTitle,
  validateTaskDescription,
  validateListTitle,
  validateDueDate,
  getErrorMessage,
} from '../validation';

describe('validation', () => {
  describe('validateTaskTitle', () => {
    it('rejects empty title', () => {
      expect(validateTaskTitle('')).toBeTruthy();
      expect(validateTaskTitle('   ')).toBeTruthy();
    });

    it('rejects title exceeding 255 chars', () => {
      const longTitle = 'a'.repeat(256);
      expect(validateTaskTitle(longTitle)).toBeTruthy();
    });

    it('accepts valid title', () => {
      expect(validateTaskTitle('Valid Task Title')).toBeFalsy();
      expect(validateTaskTitle('a')).toBeFalsy();
    });

    it('accepts title with exactly 255 chars', () => {
      const maxTitle = 'a'.repeat(255);
      expect(validateTaskTitle(maxTitle)).toBeFalsy();
    });
  });

  describe('validateTaskDescription', () => {
    it('accepts empty description (optional)', () => {
      expect(validateTaskDescription('')).toBeFalsy();
    });

    it('rejects description exceeding 5000 chars', () => {
      const longDesc = 'a'.repeat(5001);
      expect(validateTaskDescription(longDesc)).toBeTruthy();
    });

    it('accepts valid description', () => {
      expect(validateTaskDescription('A valid task description')).toBeFalsy();
    });

    it('accepts description with exactly 5000 chars', () => {
      const maxDesc = 'a'.repeat(5000);
      expect(validateTaskDescription(maxDesc)).toBeFalsy();
    });
  });

  describe('validateListTitle', () => {
    it('rejects empty list title', () => {
      expect(validateListTitle('')).toBeTruthy();
      expect(validateListTitle('   ')).toBeTruthy();
    });

    it('rejects title exceeding 255 chars', () => {
      const longTitle = 'a'.repeat(256);
      expect(validateListTitle(longTitle)).toBeTruthy();
    });

    it('accepts valid list title', () => {
      expect(validateListTitle('My List')).toBeFalsy();
      expect(validateListTitle('Work')).toBeFalsy();
    });
  });

  describe('validateDueDate', () => {
    it('accepts empty due date (optional)', () => {
      expect(validateDueDate('')).toBeFalsy();
    });

    it('rejects past dates', () => {
      expect(validateDueDate('2020-01-01')).toBeTruthy();
    });

    it('accepts today or future dates', () => {
      const today = new Date().toISOString().split('T')[0];
      expect(validateDueDate(today)).toBeFalsy();

      const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
      expect(validateDueDate(tomorrow)).toBeFalsy();
    });

    it('rejects invalid date format', () => {
      expect(validateDueDate('invalid-date')).toBeTruthy();
      expect(validateDueDate('2026/05/26')).toBeTruthy();
    });
  });

  describe('getErrorMessage', () => {
    it('returns error message for title validation', () => {
      const msg = getErrorMessage('title', 'required');
      expect(msg).toBeTruthy();
      expect(msg).toMatch(/title|required/i);
    });

    it('returns error message for description', () => {
      const msg = getErrorMessage('description', 'maxLength');
      expect(msg).toBeTruthy();
      expect(msg).toMatch(/description|max|length/i);
    });

    it('returns error message for dueDate', () => {
      const msg = getErrorMessage('dueDate', 'invalidDate');
      expect(msg).toBeTruthy();
      expect(msg).toMatch(/date|invalid|future/i);
    });

    it('returns default message for unknown error', () => {
      const msg = getErrorMessage('unknownField', 'unknownError');
      expect(msg).toBeTruthy();
    });
  });
});
