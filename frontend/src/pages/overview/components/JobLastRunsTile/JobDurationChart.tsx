import { useMemo } from 'react';
import { ChartsReferenceLine, LineChart } from '@mui/x-charts';
import { formatDuration } from '~/utils';
import type { JobRow } from '../../jobInsights';

type Props = { row: JobRow };

/** Daily average duration for one job, with its usual (median) duration as a dashed reference line. */
const JobDurationChart = ({ row }: Props) => {
  const { dates, values } = useMemo(
    () => ({
      dates: row.points.map((point) => new Date(point.date)),
      values: row.points.map((point) => point.averageSeconds),
    }),
    [row.points],
  );

  if (values.length === 0) {
    return null;
  }

  return (
    <LineChart
      height={180}
      xAxis={[{ scaleType: 'time', data: dates }]}
      yAxis={[{ min: 0, valueFormatter: (value: number) => formatDuration(value) }]}
      series={[
        {
          label: 'Daily average',
          data: values,
          showMark: values.length <= 31,
          valueFormatter: (value: number | null) => (value == null ? '—' : formatDuration(value)),
        },
      ]}
      hideLegend
      margin={{ left: 8, right: 16, top: 12, bottom: 8 }}
    >
      {row.usualSeconds !== null && (
        <ChartsReferenceLine
          y={row.usualSeconds}
          label={`usual ${formatDuration(row.usualSeconds)}`}
          labelAlign="end"
          lineStyle={{ strokeDasharray: '4 4' }}
        />
      )}
    </LineChart>
  );
};

export default JobDurationChart;
