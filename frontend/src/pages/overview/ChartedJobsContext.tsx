import { createContext, useContext } from 'react';
import type { JobDurationSeries } from '~/types';
import { categoricalPalette } from '~/theme';

export const MAX_CHARTED_JOBS = 5;

type ChartedJobsContextValue = {
  /** Jobs with duration data in the current window — the only ones that can be charted. */
  chartableJobs: string[];
  /** Jobs currently drawn on the Job Duration Trends chart (at most MAX_CHARTED_JOBS). */
  chartedJobs: string[];
  /** Line colour per charted job; shared so table rows can match their chart line. */
  colorByJob: Record<string, string>;
  toggleChartedJob: (jobName: string) => void;
};

export const ChartedJobsContext = createContext<ChartedJobsContextValue>({
  chartableJobs: [],
  chartedJobs: [],
  colorByJob: {},
  toggleChartedJob: () => {},
});

export const useChartedJobs = () => useContext(ChartedJobsContext);

const meanDailyAverage = (series: JobDurationSeries) =>
  series.points.length === 0
    ? 0
    : series.points.reduce((sum, point) => sum + point.averageSeconds, 0) / series.points.length;

/** Default selection: the longest-running jobs in the window, ties broken by name. */
export const defaultChartedJobs = (trends: JobDurationSeries[]): string[] =>
  [...trends]
    .sort((a, b) => meanDailyAverage(b) - meanDailyAverage(a) || a.jobName.localeCompare(b.jobName))
    .slice(0, MAX_CHARTED_JOBS)
    .map((series) => series.jobName);

/**
 * Each job prefers the palette slot matching its position in the full job list, so its
 * colour stays put as other jobs are toggled. With more jobs than palette colours two
 * charted jobs can want the same slot; the later one moves to the next free colour.
 */
export const assignJobColors = (chartableJobs: string[], chartedJobs: string[]): Record<string, string> => {
  const charted = new Set(chartedJobs);
  const usedColors = new Set<string>();
  const colorByJob: Record<string, string> = {};
  chartableJobs.forEach((jobName, index) => {
    if (!charted.has(jobName)) {
      return;
    }
    let slot = index % categoricalPalette.length;
    while (usedColors.has(categoricalPalette[slot])) {
      slot = (slot + 1) % categoricalPalette.length;
    }
    usedColors.add(categoricalPalette[slot]);
    colorByJob[jobName] = categoricalPalette[slot];
  });
  return colorByJob;
};
