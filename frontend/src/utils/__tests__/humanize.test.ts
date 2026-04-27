import { describe, expect, it } from 'vitest';
import { humanize } from '../humanize';

describe('humanize', () => {
  it.each([
    ['importUsersJob', 'Import Users Job'],
    ['orgChartSyncJob', 'Org Chart Sync Job'],
    ['exportCSVJob', 'Export CSV Job'],
    ['HTTPRetryJob', 'HTTP Retry Job'],
    ['snake_case_job', 'Snake Case Job'],
    ['kebab-case-job', 'Kebab Case Job'],
    ['already spaced text', 'Already Spaced Text'],
    ['simple', 'Simple'],
  ])('humanize(%s) === %s', (input, expected) => {
    expect(humanize(input)).toBe(expected);
  });

  it.each([null, undefined, ''])('humanize(%s) returns empty string', (input) => {
    expect(humanize(input)).toBe('');
  });
});
