import { describe, expect, it } from 'vitest';
import { categoricalPalette } from '~/theme';
import { assignJobColors, defaultChartedJobs } from '../ChartedJobsContext';

describe('defaultChartedJobs', () => {
  it('picks the five longest mean durations, breaking ties by name', () => {
    const trends = [
      { jobName: 'cJob', points: [{ date: '2026-04-30', averageSeconds: 50 }] },
      { jobName: 'aJob', points: [{ date: '2026-04-30', averageSeconds: 50 }] },
      { jobName: 'bJob', points: [{ date: '2026-04-29', averageSeconds: 10 }, { date: '2026-04-30', averageSeconds: 90 }] },
      { jobName: 'dJob', points: [{ date: '2026-04-30', averageSeconds: 5 }] },
      { jobName: 'eJob', points: [{ date: '2026-04-30', averageSeconds: 70 }] },
      { jobName: 'emptyJob', points: [] },
    ];

    expect(defaultChartedJobs(trends)).toEqual(['eJob', 'aJob', 'bJob', 'cJob', 'dJob']);
  });
});

describe('assignJobColors', () => {
  it('keeps each job on its list-position colour when there is no collision', () => {
    expect(assignJobColors(['aJob', 'bJob', 'cJob'], ['aJob', 'cJob'])).toEqual({
      aJob: categoricalPalette[0],
      cJob: categoricalPalette[2],
    });
  });

  it('moves a job to the next free colour when its slot wraps onto a charted job', () => {
    const jobs = Array.from({ length: categoricalPalette.length + 1 }, (_, index) => `job${index}`);
    const lastJob = jobs[jobs.length - 1];

    const colors = assignJobColors(jobs, ['job0', 'job1', lastJob]);

    expect(colors.job0).toBe(categoricalPalette[0]);
    expect(colors.job1).toBe(categoricalPalette[1]);
    expect(colors[lastJob]).toBe(categoricalPalette[2]);
  });
});
