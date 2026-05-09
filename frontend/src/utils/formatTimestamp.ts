const dateFormatter = new Intl.DateTimeFormat('en-US', {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
});

const timeFormatter = new Intl.DateTimeFormat('en-US', {
  hour: 'numeric',
  minute: '2-digit',
  hour12: true,
  timeZoneName: 'short',
});

/**
 * Formats a backend ISO-8601 UTC instant (e.g. '2026-05-05T18:00:00Z') into a friendly
 * display string like "May 5, 2026 1:00 PM CDT" rendered in the browser's local zone.
 * Returns "—" for null/undefined and falls back to the raw input if parsing fails.
 */
export const formatTimestamp = (raw: string | null | undefined): string => {
  if (raw === null || raw === undefined || raw === '') {
    return '—';
  }
  const date = new Date(raw);
  if (Number.isNaN(date.getTime())) {
    return raw;
  }
  return `${dateFormatter.format(date)} ${timeFormatter.format(date)}`;
};
