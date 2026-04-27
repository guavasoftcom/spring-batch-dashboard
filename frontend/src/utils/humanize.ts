/**
 * Convert a camelCase / PascalCase identifier into a human-readable title.
 *
 * Examples:
 *   humanize('importUsersJob')       -> 'Import Users Job'
 *   humanize('orgChartSyncJob')      -> 'Org Chart Sync Job'
 *   humanize('exportCSVJob')         -> 'Export CSV Job'
 *   humanize('HTTPRetryJob')         -> 'HTTP Retry Job'
 *   humanize('snake_case_job')       -> 'Snake Case Job'
 *   humanize('already spaced text')  -> 'Already Spaced Text'
 *   humanize(undefined)              -> ''
 */
export const humanize = (value: string | null | undefined): string => {
  if (!value) {
    return '';
  }
  const spaced = value
    .replace(/[_-]+/g, ' ')
    .replace(/([a-z\d])([A-Z])/g, '$1 $2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1 $2')
    .trim();
  return spaced
    .split(/\s+/)
    .map((word) => (word.length === 0 ? word : word[0].toUpperCase() + word.slice(1)))
    .join(' ');
};
