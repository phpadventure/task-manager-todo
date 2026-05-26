import { describe, it, expect, beforeEach, vi } from 'vitest';
import { formatDate, isOverdue, isDueSoon, getDueDateLabel } from '../dateUtils';

describe('dateUtils', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-05-26'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('formatDate', () => {
    it('formats date in readable format', () => {
      const result = formatDate('2026-05-26');
      expect(result).toMatch(/May|May 26|26\/5|5\/26/); // Handle various locale formats
    });

    it('handles null/undefined', () => {
      expect(formatDate(null)).toBe('');
      expect(formatDate(undefined)).toBe('');
    });
  });

  describe('isOverdue', () => {
    it('returns true for past dates', () => {
      expect(isOverdue('2026-05-25')).toBe(true);
      expect(isOverdue('2026-01-01')).toBe(true);
    });

    it('returns false for today and future dates', () => {
      expect(isOverdue('2026-05-26')).toBe(false);
      expect(isOverdue('2026-05-27')).toBe(false);
    });

    it('handles null/undefined', () => {
      expect(isOverdue(null)).toBe(false);
      expect(isOverdue(undefined)).toBe(false);
    });
  });

  describe('isDueSoon', () => {
    it('returns true for dates within 3 days', () => {
      expect(isDueSoon('2026-05-26')).toBe(true); // Today
      expect(isDueSoon('2026-05-27')).toBe(true); // Tomorrow
      expect(isDueSoon('2026-05-28')).toBe(true); // In 2 days
      expect(isDueSoon('2026-05-29')).toBe(true); // In 3 days
    });

    it('returns false for dates beyond 3 days', () => {
      expect(isDueSoon('2026-05-30')).toBe(false);
      expect(isDueSoon('2026-06-01')).toBe(false);
    });

    it('returns false for past dates', () => {
      expect(isDueSoon('2026-05-25')).toBe(false);
    });

    it('handles null/undefined', () => {
      expect(isDueSoon(null)).toBe(false);
      expect(isDueSoon(undefined)).toBe(false);
    });
  });

  describe('getDueDateLabel', () => {
    it('returns "Overdue" for past dates', () => {
      expect(getDueDateLabel('2026-05-25')).toBe('Overdue');
    });

    it('returns "Today" for current date', () => {
      expect(getDueDateLabel('2026-05-26')).toBe('Today');
    });

    it('returns "Tomorrow" for next day', () => {
      expect(getDueDateLabel('2026-05-27')).toBe('Tomorrow');
    });

    it('returns "In X days" for upcoming dates', () => {
      const label = getDueDateLabel('2026-05-28');
      expect(label).toMatch(/In \d+ day|days/);
    });

    it('handles null/undefined', () => {
      expect(getDueDateLabel(null)).toBe('');
      expect(getDueDateLabel(undefined)).toBe('');
    });
  });
});
