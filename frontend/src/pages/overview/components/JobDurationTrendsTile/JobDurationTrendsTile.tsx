import { useMemo } from 'react';
import { Box, Typography } from '@mui/material';
import { LineChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import type { JobDurationSeries } from '~/types';
import { formatDuration, humanize } from '~/utils';

type Props = {
  data: JobDurationSeries[] | null;
  loading: boolean;
  error: string | null;
  chartedJobs: string[];
  colorByJob: Record<string, string>;
};

const JobDurationTrendsTile = ({ data, loading, error, chartedJobs, colorByJob }: Props) => {
  const chartedSeries = useMemo(
    () => (data ?? []).filter((series) => chartedJobs.includes(series.jobName)),
    [data, chartedJobs],
  );

  // Each job's points may cover a different subset of days in the window. Build the union
  // of dates across all jobs, sort ascending, then align every series to that shared axis
  // by inserting `null` where a job didn't run that day. MUI x-charts breaks the line at
  // null gaps automatically, so days without runs read as a visible interruption.
  const { dates, lineSeries } = useMemo(() => {
    const dateSet = new Set<string>();
    chartedSeries.forEach((s) => s.points.forEach((p) => dateSet.add(p.date)));
    const sortedDates = Array.from(dateSet).sort();
    return {
      dates: sortedDates.map((d) => new Date(d)),
      lineSeries: chartedSeries.map((s) => {
        const byDate = new Map(s.points.map((p) => [p.date, p.averageSeconds]));
        return {
          label: humanize(s.jobName),
          color: colorByJob[s.jobName],
          data: sortedDates.map((d) => byDate.get(d) ?? null),
          connectNulls: true,
          valueFormatter: (value: number | null) => (value == null ? '—' : formatDuration(value)),
        };
      }),
    };
  }, [chartedSeries, colorByJob]);

  return (
    <LargeTile
      title="Job Duration Trends"
      headerAction={
        data && data.length > 0 && (
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Showing {chartedSeries.length} of {data.length} jobs
          </Typography>
        )
      }
      loading={loading}
      error={error}
      minHeight={340}
      // Replace the animated skeleton with an invisible spacer of the chart's
      // exact height. The default 260px skeleton vs. 300px chart created a
      // 40px layout jump when the swap happened, which made MUI x-charts
      // measure mid-transition and skip its enter animation.
      loadingSkeleton={<Box sx={{ height: 300 }} />}
    >
      {data && data.length > 0 && chartedSeries.length === 0 && (
        <Typography sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>
          Select jobs in Last Run by Job to chart them.
        </Typography>
      )}
      {chartedSeries.length > 0 && (
        <LineChart
          height={300}
          xAxis={[{ scaleType: 'time', data: dates }]}
          yAxis={[{ min: 0, label: 'Avg duration (s)' }]}
          series={lineSeries}
          margin={20}
          slotProps={{ tooltip: { trigger: 'item' } }}
        />
      )}
      {data && data.length === 0 && (
        <Typography sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>
          No completed runs in this window.
        </Typography>
      )}
    </LargeTile>
  );
};

export default JobDurationTrendsTile;
