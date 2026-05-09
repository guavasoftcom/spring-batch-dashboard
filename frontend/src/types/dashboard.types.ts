import type { JobRun } from './jobRuns.types';

export type ExecutionCounts = {
  total: number;
  completed: number;
  failed: number;
  started: number;
};

export type ThroughputSummary = {
  readCount: number;
  writeCount: number;
};

export type Durations = {
  averageSeconds: number;
  longestSeconds: number;
};

export type JobDurationPoint = { date: string; averageSeconds: number };

export type JobDurationSeries = {
  jobName: string;
  points: JobDurationPoint[];
};

export type StepCountsSummary = {
  readCount: number;
  writeCount: number;
  commitCount: number;
  filterCount: number;
  readSkipCount: number;
  writeSkipCount: number;
  processSkipCount: number;
  rollbackCount: number;
};

export type JobLastRun = {
  jobName: string;
  run: JobRun | null;
};
