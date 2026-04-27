import type { JobRun } from '~/types';

export const sampleRuns: JobRun[] = [
  { executionId: 5142, status: 'STARTED',   startTime: '2026-04-24 09:25:10', endTime: null,                  durationSeconds: 209, readCount: 14952, writeCount: 14910, exitCode: 'EXECUTING' },
  { executionId: 5141, status: 'FAILED',    startTime: '2026-04-23 23:00:01', endTime: '2026-04-23 23:03:18', durationSeconds: 197, readCount: 5042,  writeCount: 4870,  exitCode: 'FAILED' },
  { executionId: 5140, status: 'COMPLETED', startTime: '2026-04-22 23:00:01', endTime: '2026-04-22 23:02:42', durationSeconds: 161, readCount: 5012,  writeCount: 5012,  exitCode: 'COMPLETED' },
  { executionId: 5139, status: 'COMPLETED', startTime: '2026-04-21 23:00:01', endTime: '2026-04-21 23:02:51', durationSeconds: 170, readCount: 4998,  writeCount: 4998,  exitCode: 'COMPLETED' },
  { executionId: 5138, status: 'COMPLETED', startTime: '2026-04-20 23:00:01', endTime: '2026-04-20 23:03:05', durationSeconds: 184, readCount: 5101,  writeCount: 5101,  exitCode: 'COMPLETED' },
];

export const computeRunCounts = (runs: JobRun[]) => {
  const completed = runs.filter((r) => r.status === 'COMPLETED').length;
  const failed = runs.filter((r) => r.status === 'FAILED').length;
  const finished = runs.filter((r) => r.status !== 'STARTED').length;
  return { total: runs.length, completed, failed, finished };
};

export const computeSuccessRate = (runs: JobRun[]) => {
  const { completed, finished } = computeRunCounts(runs);
  return {
    successRate: finished ? Math.round((completed / finished) * 100) : 0,
    completed,
    finished,
  };
};

export const computeAvgDuration = (runs: JobRun[]) => {
  const finishedRuns = runs.filter((r) => r.status !== 'STARTED');
  return {
    averageSeconds: finishedRuns.length
      ? Math.round(finishedRuns.reduce((acc, r) => acc + r.durationSeconds, 0) / finishedRuns.length)
      : 0,
  };
};

export const filterTrendRuns = (runs: JobRun[], windowDays: number) => {
  const sorted = [...runs].sort(
    (a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime(),
  );
  if (sorted.length === 0) {
    return sorted;
  }
  const latest = new Date(sorted[sorted.length - 1].startTime).getTime();
  const cutoff = latest - windowDays * 24 * 60 * 60 * 1000;
  return sorted.filter((r) => new Date(r.startTime).getTime() >= cutoff);
};
