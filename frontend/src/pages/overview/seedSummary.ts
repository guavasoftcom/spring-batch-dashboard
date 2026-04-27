import type {
  Durations,
  ExecutionCounts,
  JobStatusSlice,
  QualitySignals,
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

export const qualitySignalsMock: QualitySignals = {
  lastFailure: 'reconcileLedgerJob / reconcileStep',
  processing: { readCount: 5042, writeCount: 4998, commitCount: 50, filterCount: 3, rollbackCount: 1, skipCount: 3 },
  latestUpdate: '2026-04-24 09:30:30',
};
