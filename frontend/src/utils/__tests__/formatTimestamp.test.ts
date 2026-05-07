import { describe, expect, it } from 'vitest';
import { formatTimestamp } from '~/utils/formatTimestamp';

describe('formatTimestamp', () => {
  it('formats a backend timestamp into "Mon D, YYYY h:mm AM/PM"', () => {
    expect(formatTimestamp('2026-05-05 18:00:00')).toBe('May 5, 2026 6:00 PM');
  });

  it('formats midnight as 12:00 AM', () => {
    expect(formatTimestamp('2026-05-05 00:00:00')).toBe('May 5, 2026 12:00 AM');
  });

  it('zero-pads minutes', () => {
    expect(formatTimestamp('2026-05-05 06:05:00')).toBe('May 5, 2026 6:05 AM');
  });

  it('returns em dash for null', () => {
    expect(formatTimestamp(null)).toBe('—');
  });

  it('returns em dash for undefined', () => {
    expect(formatTimestamp(undefined)).toBe('—');
  });

  it('returns em dash for empty string', () => {
    expect(formatTimestamp('')).toBe('—');
  });

  it('falls back to the raw input when parsing fails', () => {
    expect(formatTimestamp('not-a-date')).toBe('not-a-date');
  });
});
