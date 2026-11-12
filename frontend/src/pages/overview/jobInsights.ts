import type { JobDurationSeries, JobLastRun, JobRun } from '~/types';

/** A last run this much slower than the job's usual duration counts as "slower than usual". */
export const SLOWER_THRESHOLD = 0.25;

export type JobFilter = 'all' | 'failed' | 'running' | 'slower' | 'completed';
export type JobSort = 'attention' | 'slowdown' | 'duration' | 'name';

export type JobRow = {
  jobName: string;
  run: JobRun | null;
  /** Daily average durations in the window, ascending by date. Empty when the job has no completed runs. */
  points: JobDurationSeries['points'];
  /** Median of the daily averages: the job's "usual" duration. Null without completed runs. */
  usualSeconds: number | null;
  /** Last run's duration relative to usual (0.25 = 25% slower). Null unless the last run completed. */
  changeVsUsual: number | null;
};

export const median = (values: number[]): number | null => {
  if (values.length === 0) {
    return null;
  }
  const sorted = [...values].sort((a, b) => a - b);
  const middle = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 1 ? sorted[middle] : (sorted[middle - 1] + sorted[middle]) / 2;
};

export const buildJobRows = (lastRuns: JobLastRun[], trends: JobDurationSeries[]): JobRow[] => {
  const pointsByJob = new Map(trends.map((series) => [series.jobName, series.points]));
  return lastRuns.map(({ jobName, run }) => {
    const points = [...(pointsByJob.get(jobName) ?? [])].sort((a, b) => a.date.localeCompare(b.date));
    const usualSeconds = median(points.map((point) => point.averageSeconds));
    const changeVsUsual =
      run?.status === 'COMPLETED' && usualSeconds
        ? run.durationSeconds / usualSeconds - 1
        : null;
    return { jobName, run, points, usualSeconds, changeVsUsual };
  });
};

export const isSlower = (row: JobRow) => row.changeVsUsual !== null && row.changeVsUsual > SLOWER_THRESHOLD;

const FILTER_PREDICATES: Record<JobFilter, (row: JobRow) => boolean> = {
  all: () => true,
  failed: (row) => row.run?.status === 'FAILED',
  running: (row) => row.run?.status === 'STARTED',
  slower: isSlower,
  completed: (row) => row.run?.status === 'COMPLETED',
};

export const matchesFilter = (row: JobRow, filter: JobFilter) => FILTER_PREDICATES[filter](row);

export const countByFilter = (rows: JobRow[]): Record<JobFilter, number> => ({
  all: rows.length,
  failed: rows.filter(FILTER_PREDICATES.failed).length,
  running: rows.filter(FILTER_PREDICATES.running).length,
  slower: rows.filter(FILTER_PREDICATES.slower).length,
  completed: rows.filter(FILTER_PREDICATES.completed).length,
});

/** Lower sorts first: failed, running, slower than usual, everything else, then jobs with no run. */
const attentionRank = (row: JobRow) => {
  if (!row.run) {
    return 4;
  }
  if (row.run.status === 'FAILED') {
    return 0;
  }
  if (row.run.status === 'STARTED') {
    return 1;
  }
  return isSlower(row) ? 2 : 3;
};

const byName = (a: JobRow, b: JobRow) => a.jobName.localeCompare(b.jobName);
const startedAt = (row: JobRow) => (row.run ? Date.parse(row.run.startTime) : 0);

const COMPARATORS: Record<JobSort, (a: JobRow, b: JobRow) => number> = {
  attention: (a, b) => attentionRank(a) - attentionRank(b) || startedAt(b) - startedAt(a) || byName(a, b),
  slowdown: (a, b) => (b.changeVsUsual ?? -Infinity) - (a.changeVsUsual ?? -Infinity) || byName(a, b),
  duration: (a, b) => (b.run?.durationSeconds ?? -1) - (a.run?.durationSeconds ?? -1) || byName(a, b),
  name: byName,
};

export const filterAndSortRows = (rows: JobRow[], filter: JobFilter, query: string, sort: JobSort): JobRow[] => {
  const needle = query.trim().toLowerCase();
  return rows
    .filter((row) => matchesFilter(row, filter) && (!needle || row.jobName.toLowerCase().includes(needle)))
    .sort(COMPARATORS[sort]);
};
