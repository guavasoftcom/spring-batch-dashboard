export type RunStatus = 'COMPLETED' | 'FAILED' | 'STARTED';

export type JobRun = {
  executionId: number;
  status: RunStatus;
  startTime: string;
  endTime: string | null;
  durationSeconds: number;
  readCount: number;
  writeCount: number;
  exitCode: string;
};

export type RunCounts = {
  total: number;
  completed: number;
  failed: number;
  finished: number;
};

export type SuccessRate = {
  successRate: number;
  completed: number;
  finished: number;
};

export type AvgDuration = {
  averageSeconds: number;
};
