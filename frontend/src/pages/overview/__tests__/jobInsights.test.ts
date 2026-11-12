import { describe, expect, it } from 'vitest';
import type { JobLastRun } from '~/types';
import { buildJobRows, countByFilter, filterAndSortRows, median } from '../jobInsights';

const completedRun = (durationSeconds: number): JobLastRun['run'] => ({
  executionId: 1,
  status: 'COMPLETED',
  startTime: '2026-04-30T09:00:00Z',
  endTime: '2026-04-30T09:10:00Z',
  durationSeconds,
  readCount: 1,
  writeCount: 1,
  exitCode: 'COMPLETED',
});

describe('median', () => {
  it('handles empty, odd and even lists', () => {
    expect(median([])).toBeNull();
    expect(median([5, 1, 3])).toBe(3);
    expect(median([4, 1, 3, 2])).toBe(2.5);
  });
});

describe('buildJobRows', () => {
  it('derives usual duration and change from the daily averages, sorting points by date', () => {
    const [row] = buildJobRows(
      [{ jobName: 'aJob', run: completedRun(150) }],
      [{
        jobName: 'aJob',
        points: [
          { date: '2026-04-30', averageSeconds: 300 },
          { date: '2026-04-28', averageSeconds: 100 },
          { date: '2026-04-29', averageSeconds: 100 },
        ],
      }],
    );

    expect(row.points.map((point) => point.date)).toEqual(['2026-04-28', '2026-04-29', '2026-04-30']);
    expect(row.usualSeconds).toBe(100);
    expect(row.changeVsUsual).toBeCloseTo(0.5);
  });

  it('has no change for a job without trend data or whose last run did not complete', () => {
    const rows = buildJobRows(
      [
        { jobName: 'noTrendJob', run: completedRun(10) },
        { jobName: 'failedJob', run: { ...completedRun(10)!, status: 'FAILED' } },
        { jobName: 'idleJob', run: null },
      ],
      [{ jobName: 'failedJob', points: [{ date: '2026-04-30', averageSeconds: 5 }] }],
    );

    expect(rows.map((row) => row.changeVsUsual)).toEqual([null, null, null]);
    expect(rows[0].usualSeconds).toBeNull();
  });
});

describe('filterAndSortRows / countByFilter', () => {
  const rows = buildJobRows(
    [
      { jobName: 'bJob', run: completedRun(100) },
      { jobName: 'aJob', run: completedRun(300) },
      { jobName: 'cJob', run: completedRun(50) },
    ],
    ['aJob', 'bJob', 'cJob'].map((jobName) => ({ jobName, points: [{ date: '2026-04-30', averageSeconds: 100 }] })),
  );

  it('counts jobs per filter', () => {
    expect(countByFilter(rows)).toEqual({ all: 3, failed: 0, running: 0, slower: 1, completed: 3 });
  });

  it('sorts by slowdown, duration and name', () => {
    const names = (sort: Parameters<typeof filterAndSortRows>[3]) =>
      filterAndSortRows(rows, 'all', '', sort).map((row) => row.jobName);

    expect(names('slowdown')).toEqual(['aJob', 'bJob', 'cJob']);
    expect(names('duration')).toEqual(['aJob', 'bJob', 'cJob']);
    expect(names('name')).toEqual(['aJob', 'bJob', 'cJob']);
  });

  it('matches the search text case-insensitively', () => {
    expect(filterAndSortRows(rows, 'all', ' BJ', 'name').map((row) => row.jobName)).toEqual(['bJob']);
  });
});
