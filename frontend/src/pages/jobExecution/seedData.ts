import type {
  DurationSummary,
  IoSummary,
  JobExecutionStepCounts,
  StepDuration,
  StepRow,
} from './types';

export const sampleSteps: StepRow[] = [
  {
    id: 1, stepName: 'extractStep', status: 'COMPLETED',
    readCount: 5042, writeCount: 5042, skipCount: 0, rollbackCount: 0,
    durationSeconds: 42, startTime: '2026-04-24 09:25:10', endTime: '2026-04-24 09:25:52',
    exitCode: 'COMPLETED', exitMessage: null,
    context: { source: 'orders.csv', batchSize: 500, fetchSize: 1000 },
  },
  {
    id: 2, stepName: 'transformStep', status: 'COMPLETED',
    readCount: 5042, writeCount: 4998, skipCount: 3, rollbackCount: 0,
    durationSeconds: 88, startTime: '2026-04-24 09:25:52', endTime: '2026-04-24 09:27:20',
    exitCode: 'COMPLETED', exitMessage: null,
    context: { processor: 'OrderEnrichmentProcessor', skipPolicy: 'AlwaysSkip', skipLimit: 10 },
  },
  {
    id: 3, stepName: 'reconcileStep', status: 'FAILED',
    readCount: 4998, writeCount: 4870, skipCount: 0, rollbackCount: 1,
    durationSeconds: 67, startTime: '2026-04-24 09:27:20', endTime: '2026-04-24 09:28:27',
    exitCode: 'FAILED',
    exitMessage: 'org.springframework.dao.DataIntegrityViolationException: duplicate key value violates unique constraint "ledger_entry_pkey"',
    context: { ledgerSource: 'gl_2026_04', tolerance: 0.01, retryAttempts: 3 },
  },
  {
    id: 4, stepName: 'loadStep', status: 'STARTED',
    readCount: 4870, writeCount: 0, skipCount: 0, rollbackCount: 0,
    durationSeconds: 12, startTime: '2026-04-24 09:28:27', endTime: null,
    exitCode: 'EXECUTING', exitMessage: null,
    context: { target: 'datamart.fact_orders', commitInterval: 200 },
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

export const computeStepDurations = (steps: StepRow[]): StepDuration[] =>
  steps.map((s) => ({ stepName: s.stepName, durationSeconds: s.durationSeconds }));
