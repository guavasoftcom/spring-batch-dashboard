import type {
  Durations,
  ExecutionCounts,
  JobDurationSeries,
  JobLastRun,
  ThroughputSummary,
} from '~/types';

export const jobCountsMock: ExecutionCounts = { total: 4, completed: 2, failed: 1, started: 1 };
export const stepCountsMock: ExecutionCounts = { total: 6, completed: 4, failed: 1, started: 1 };
export const throughputMock: ThroughputSummary = { readCount: 5042, writeCount: 4998 };
export const runtimeMock: Durations = { averageSeconds: 197, longestSeconds: 341 };

export const jobDurationTrendsMock: JobDurationSeries[] = [
  {
    jobName: 'archiveOrdersJob',
    points: [
      { date: '2026-04-26', averageSeconds: 88 },
      { date: '2026-04-27', averageSeconds: 92 },
      { date: '2026-04-28', averageSeconds: 95 },
      { date: '2026-04-29', averageSeconds: 90 },
      { date: '2026-04-30', averageSeconds: 96 },
    ],
  },
  {
    jobName: 'importCustomersJob',
    points: [
      { date: '2026-04-26', averageSeconds: 201 },
      { date: '2026-04-27', averageSeconds: 215 },
      { date: '2026-04-29', averageSeconds: 198 },
      { date: '2026-04-30', averageSeconds: 209 },
    ],
  },
  {
    jobName: 'reconcileLedgerJob',
    points: [
      { date: '2026-04-27', averageSeconds: 45 },
      { date: '2026-04-28', averageSeconds: 52 },
      { date: '2026-04-30', averageSeconds: 60 },
    ],
  },
];

export const jobLastRunsMock: JobLastRun[] = [
  {
    jobName: 'reconcileLedgerJob',
    run: {
      executionId: 4321,
      status: 'FAILED',
      startTime: '2026-04-30T09:15:30Z',
      endTime: '2026-04-30T09:16:30Z',
      durationSeconds: 60,
      readCount: 1042,
      writeCount: 0,
      exitCode: 'FAILED',
    },
  },
  {
    jobName: 'importCustomersJob',
    run: {
      executionId: 4318,
      status: 'COMPLETED',
      startTime: '2026-04-30T06:00:00Z',
      endTime: '2026-04-30T06:03:21Z',
      durationSeconds: 201,
      readCount: 5042,
      writeCount: 4998,
      exitCode: 'COMPLETED',
    },
  },
  { jobName: 'archiveOrdersJob', run: null },
];
