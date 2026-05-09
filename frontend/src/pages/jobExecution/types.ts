export type StepStatus = 'COMPLETED' | 'FAILED' | 'STARTED';

export type StepRow = {
  id: number;
  stepName: string;
  status: StepStatus;
  readCount: number;
  writeCount: number;
  skipCount: number;
  rollbackCount: number;
  durationSeconds: number;
  startTime: string;
  endTime: string | null;
};

export type JobExecutionStepCounts = {
  totalSteps: number;
  completed: number;
  failed: number;
  active: number;
};

export type IoSummary = {
  totalRead: number;
  totalWrite: number;
};

export type DurationSummary = {
  totalDurationSeconds: number;
};

export type JobExecutionTiming = {
  createTime: string;
  startTime: string | null;
  endTime: string | null;
};

export type StepDetailPage = {
  content: StepRow[];
  page: number;
  size: number;
  totalElements: number;
};

export type StepExecutionDetail = {
  id: number;
  jobExecutionId: number;
  stepName: string;
  status: StepStatus;
  readCount: number;
  writeCount: number;
  commitCount: number;
  filterCount: number;
  readSkipCount: number;
  writeSkipCount: number;
  processSkipCount: number;
  rollbackCount: number;
  durationSeconds: number;
  createTime: string;
  startTime: string | null;
  endTime: string | null;
  lastUpdated: string | null;
  exitCode: string | null;
  exitMessage: string | null;
  executionContext: Record<string, unknown>;
};
