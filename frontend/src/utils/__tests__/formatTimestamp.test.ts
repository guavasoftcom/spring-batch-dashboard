import { describe, expect, it } from 'vitest';
import { formatTimestamp } from '~/utils/formatTimestamp';

// Format assertions are zone-dependent (the function uses the browser's local zone),
// so the tests check the *shape* of the output rather than literal strings, plus a
// round-trip identity for relative ordering of two instants. The trailing token is the
// abbreviated time-zone name (e.g. "CDT", "GMT", "GMT-5").
const FORMAT_PATTERN = /^[A-Z][a-z]{2} \d{1,2}, \d{4} \d{1,2}:\d{2} (AM|PM) \S+/;

describe('formatTimestamp', () => {
  it('formats an ISO-8601 UTC instant into "Mon D, YYYY h:mm AM/PM TZ"', () => {
    expect(formatTimestamp('2026-05-05T18:00:00Z')).toMatch(FORMAT_PATTERN);
  });

  it('formats midnight UTC into the host zone', () => {
    expect(formatTimestamp('2026-05-05T00:00:00Z')).toMatch(FORMAT_PATTERN);
  });

  it('produces different output for two different instants', () => {
    const morning = formatTimestamp('2026-05-05T06:05:00Z');
    const evening = formatTimestamp('2026-05-05T18:00:00Z');
    expect(morning).not.toEqual(evening);
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
