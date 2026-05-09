import { BarChart } from '@mui/x-charts';
import type { StepCountsSummary } from '~/types';

const BAR_COLORS = ['#42A5F5', '#66BB6A', '#26A69A', '#AB47BC', '#FFA726', '#FF7043', '#EC407A', '#EF5350'];

type Count = { label: string; value: number };

const buildCounts = (data: StepCountsSummary): Count[] => [
  { label: 'Read', value: data.readCount },
  { label: 'Write', value: data.writeCount },
  { label: 'Commits', value: data.commitCount },
  { label: 'Filtered', value: data.filterCount },
  { label: 'Read skips', value: data.readSkipCount },
  { label: 'Write skips', value: data.writeSkipCount },
  { label: 'Process skips', value: data.processSkipCount },
  { label: 'Rollbacks', value: data.rollbackCount },
];

const StepCountsBarChart = ({ data }: { data: StepCountsSummary }) => {
  const counts = buildCounts(data);
  return (
    <BarChart
      height={300}
      xAxis={[{ scaleType: 'band', data: counts.map((c) => c.label), categoryGapRatio: 0.3 }]}
      yAxis={[{ min: 0 }]}
      series={counts.map((c, idx) => ({
        // Embed the value in the series label so it shows up in the legend.
        label: `${c.label}: ${c.value.toLocaleString()}`,
        color: BAR_COLORS[idx % BAR_COLORS.length],
        stack: 'total',
        data: counts.map((_, dataIdx) => (dataIdx === idx ? c.value : 0)),
        // The label already carries the count; suppress the tooltip's value cell
        // so it doesn't render the same number twice.
        valueFormatter: () => '',
      }))}
      margin={20}
      slotProps={{ tooltip: { trigger: 'item' } }}
    />
  );
};

export default StepCountsBarChart;
