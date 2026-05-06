import type {
  Durations,
  ExecutionCounts,
  JobLastRun,
  JobStatusSlice,
  ThroughputBar,
  ThroughputSummary,
} from '~/types';

export const jobCountsMock: ExecutionCounts = { total: 4, completed: 2, failed: 1, started: 1 };
export const stepCountsMock: ExecutionCounts = { total: 6, completed: 4, failed: 1, started: 1 };
export const throughputMock: ThroughputSummary = { readCount: 5042, writeCount: 4998 };
export const runtimeMock: Durations = { averageSeconds: 197, longestSeconds: 341 };

export const jobStatusChartMock: JobStatusSlice[] = [
  { id: 0, label: 'Completed', value: 2, color: '#4CAF50' },
  { id: 1, label: 'Failed', value: 1, color: '#F57C00' },
  { id: 2, label: 'Started', value: 1, color: '#42A5F5' },
];

export const processingMetricsMock: ThroughputBar[] = [
  { metric: 'Read', value: 5042 },
  { metric: 'Write', value: 4998 },
  { metric: 'Commits', value: 50 },
  { metric: 'Skips', value: 3 },
  { metric: 'Rollbacks', value: 1 },
];

export const jobLastRunsMock: JobLastRun[] = [
  {
    jobName: 'reconcileLedgerJob',
    run: {
      executionId: 4321,
      status: 'FAILED',
      startTime: '2026-04-30 09:15:30',
      endTime: '2026-04-30 09:16:30',
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
      startTime: '2026-04-30 06:00:00',
      endTime: '2026-04-30 06:03:21',
      durationSeconds: 201,
      readCount: 5042,
      writeCount: 4998,
      exitCode: 'COMPLETED',
    },
  },
  { jobName: 'archiveOrdersJob', run: null },
];
