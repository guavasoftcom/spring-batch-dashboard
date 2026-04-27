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

export type JobStatusSlice = { id: number; label: string; value: number; color: string };

export type ThroughputBar = { metric: string; value: number };

export type ProcessingTotals = {
  readCount: number;
  writeCount: number;
  commitCount: number;
  filterCount: number;
  rollbackCount: number;
  skipCount: number;
};

export type QualitySignals = {
  lastFailure: string | null;
  processing: ProcessingTotals;
  latestUpdate: string | null;
};
