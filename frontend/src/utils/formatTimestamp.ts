const dateFormatter = new Intl.DateTimeFormat('en-US', {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
});

const timeFormatter = new Intl.DateTimeFormat('en-US', {
  hour: 'numeric',
  minute: '2-digit',
  hour12: true,
});

/**
 * Formats a backend timestamp ('yyyy-MM-dd HH:mm:ss', interpreted as local time) into
 * a friendly display string like "May 5, 2026 6:00 PM". Returns "—" for null/undefined
 * and falls back to the raw input if parsing fails.
 */
export const formatTimestamp = (raw: string | null | undefined): string => {
  if (raw === null || raw === undefined || raw === '') {
    return '—';
  }
  // Backend emits LocalDateTime as 'yyyy-MM-dd HH:mm:ss' (no tz). Swap space → 'T' so the
  // browser parses it as local time, matching the JVM's local-time semantics.
  const date = new Date(raw.replace(' ', 'T'));
  if (Number.isNaN(date.getTime())) {
    return raw;
  }
  return `${dateFormatter.format(date)} ${timeFormatter.format(date)}`;
};
