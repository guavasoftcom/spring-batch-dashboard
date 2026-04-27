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
  exitCode: string;
  exitMessage: string | null;
  context: Record<string, string | number>;
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

export type StepDuration = {
  stepName: string;
  durationSeconds: number;
};

export type StepDetailPage = {
  content: StepRow[];
  page: number;
  size: number;
  totalElements: number;
};
