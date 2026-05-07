import { describe, expect, it } from 'vitest';
import { formatDuration } from '~/utils';

describe('formatDuration', () => {
  it.each([
    [0, '0s'],
    [5, '5s'],
    [59, '59s'],
    [60, '1m'],
    [65, '1m 5s'],
    [119, '1m 59s'],
    [120, '2m'],
    [600, '10m'],
    [3599, '59m 59s'],
    [3600, '1h'],
    [3660, '1h 1m'],
    [3665, '1h 1m'],
    [7320, '2h 2m'],
    [36000, '10h'],
  ])('formats %d seconds as %s', (input, expected) => {
    expect(formatDuration(input)).toBe(expected);
  });

  it('returns em-dash for null and undefined', () => {
    expect(formatDuration(null)).toBe('—');
    expect(formatDuration(undefined)).toBe('—');
  });

  it('returns em-dash for NaN', () => {
    expect(formatDuration(Number.NaN)).toBe('—');
  });

  it('clamps negative values to 0s', () => {
    expect(formatDuration(-5)).toBe('0s');
  });

  it('floors fractional seconds', () => {
    expect(formatDuration(59.9)).toBe('59s');
    expect(formatDuration(60.4)).toBe('1m');
  });
});
