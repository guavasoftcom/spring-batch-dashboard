import { useMemo } from 'react';
import { Typography } from '@mui/material';
import { LineChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import type { JobDurationSeries } from '~/types';
import { formatDuration, humanize } from '~/utils';

import { categoricalPalette } from '~/theme';

type Props = {
  data: JobDurationSeries[] | null;
  loading: boolean;
  error: string | null;
};

const JobDurationTrendsTile = ({ data, loading, error }: Props) => {
  // Each job's points may cover a different subset of days in the window. Build the union
  // of dates across all jobs, sort ascending, then align every series to that shared axis
  // by inserting `null` where a job didn't run that day. MUI x-charts breaks the line at
  // null gaps automatically, so days without runs read as a visible interruption.
  const { dates, lineSeries } = useMemo(() => {
    if (!data) {
      return { dates: [] as Date[], lineSeries: [] };
    }
    const dateSet = new Set<string>();
    data.forEach((s) => s.points.forEach((p) => dateSet.add(p.date)));
    const sortedDates = Array.from(dateSet).sort();
    return {
      dates: sortedDates.map((d) => new Date(d)),
      lineSeries: data.map((s, idx) => {
        const byDate = new Map(s.points.map((p) => [p.date, p.averageSeconds]));
        return {
          label: humanize(s.jobName),
          color: categoricalPalette[idx % categoricalPalette.length],
          data: sortedDates.map((d) => byDate.get(d) ?? null),
          connectNulls: true,
          valueFormatter: (value: number | null) => (value == null ? '—' : formatDuration(value)),
        };
      }),
    };
  }, [data]);

  return (
    <LargeTile
      title="Job Duration Trends"
      loading={loading}
      error={error}
      minHeight={340}
      loadingHeight={260}
    >
      {data && data.length > 0 && (
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
