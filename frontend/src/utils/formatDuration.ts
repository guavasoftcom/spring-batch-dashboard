/**
 * Formats a duration (in seconds) into a compact human-readable string.
 *
 * - < 1 min:   "Ns"           (e.g. 0 → "0s",   45 → "45s")
 * - < 1 hour:  "Nm Ms"        (e.g. 60 → "1m",  90 → "1m 30s")
 * - >= 1 hour: "Nh Mm"        (e.g. 3600 → "1h", 3660 → "1h 1m"; seconds dropped)
 *
 * Trailing zero units are omitted (60 → "1m", not "1m 0s"). Returns "—" for
 * null/undefined; clamps negatives to 0; floors fractional seconds.
 */
export const formatDuration = (totalSeconds: number | null | undefined): string => {
  if (totalSeconds === null || totalSeconds === undefined || Number.isNaN(totalSeconds)) {
    return '—';
  }
  const safe = Math.max(0, Math.floor(totalSeconds));

  if (safe < 60) {
    return `${safe}s`;
  }
  if (safe < 3600) {
    const minutes = Math.floor(safe / 60);
    const seconds = safe % 60;
    return seconds === 0 ? `${minutes}m` : `${minutes}m ${seconds}s`;
  }
  const hours = Math.floor(safe / 3600);
  const minutes = Math.floor((safe % 3600) / 60);
  return minutes === 0 ? `${hours}h` : `${hours}h ${minutes}m`;
};
