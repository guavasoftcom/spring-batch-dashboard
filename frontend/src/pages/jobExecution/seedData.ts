import type { StepCountsSummary } from '~/types';
import type {
  DurationSummary,
  IoSummary,
  JobExecutionStepCounts,
  JobExecutionTiming,
  StepRow,
} from './types';

export const sampleSteps: StepRow[] = [
  {
    id: 1, stepName: 'extractStep', status: 'COMPLETED',
    readCount: 5042, writeCount: 5042, skipCount: 0, rollbackCount: 0,
    durationSeconds: 42, startTime: '2026-04-24T09:25:10Z', endTime: '2026-04-24T09:25:52Z',
  },
  {
    id: 2, stepName: 'transformStep', status: 'COMPLETED',
    readCount: 5042, writeCount: 4998, skipCount: 3, rollbackCount: 0,
    durationSeconds: 88, startTime: '2026-04-24T09:25:52Z', endTime: '2026-04-24T09:27:20Z',
  },
  {
    id: 3, stepName: 'reconcileStep', status: 'FAILED',
    readCount: 4998, writeCount: 4870, skipCount: 0, rollbackCount: 1,
    durationSeconds: 67, startTime: '2026-04-24T09:27:20Z', endTime: '2026-04-24T09:28:27Z',
  },
  {
    id: 4, stepName: 'loadStep', status: 'STARTED',
    readCount: 4870, writeCount: 0, skipCount: 0, rollbackCount: 0,
    durationSeconds: 12, startTime: '2026-04-24T09:28:27Z', endTime: null,
  },
];

export const computeStepCounts = (steps: StepRow[]): JobExecutionStepCounts => ({
  totalSteps: steps.length,
  completed: steps.filter((s) => s.status === 'COMPLETED').length,
  failed: steps.filter((s) => s.status === 'FAILED').length,
  active: steps.filter((s) => s.status === 'STARTED').length,
});

export const computeIoSummary = (steps: StepRow[]): IoSummary => ({
  totalRead: steps.reduce((acc, s) => acc + s.readCount, 0),
  totalWrite: steps.reduce((acc, s) => acc + s.writeCount, 0),
});

export const computeDurationSummary = (steps: StepRow[]): DurationSummary => ({
  totalDurationSeconds: steps.reduce((acc, s) => acc + s.durationSeconds, 0),
});

export const sampleExecutionTiming: JobExecutionTiming = {
  createTime: '2026-04-24T09:25:00Z',
  startTime: '2026-04-24T09:25:10Z',
  endTime: '2026-04-24T09:28:39Z',
};

export const computeStepCountsSummary = (steps: StepRow[]): StepCountsSummary => ({
  readCount: steps.reduce((acc, s) => acc + s.readCount, 0),
  writeCount: steps.reduce((acc, s) => acc + s.writeCount, 0),
  commitCount: steps.reduce((acc, s) => acc + Math.round(s.readCount / 100), 0),
  filterCount: 0,
  readSkipCount: 0,
  writeSkipCount: steps.reduce((acc, s) => acc + s.skipCount, 0),
  processSkipCount: 0,
  rollbackCount: steps.reduce((acc, s) => acc + s.rollbackCount, 0),
});
