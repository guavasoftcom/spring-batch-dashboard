import { BarChart } from '@mui/x-charts';
import type { Theme } from '@mui/material/styles';
import { LargeTile } from '~/components';
import type { ThroughputBar } from '~/types';

const colors = ['#42A5F5', '#66BB6A', '#FFA726', '#AB47BC', '#EF5350'];

type Props = {
  data: ThroughputBar[] | null;
  loading: boolean;
  error: string | null;
};

const ProcessingMetricsTile = ({ data, loading, error }: Props) => (
  <LargeTile title="Processing Metrics" loading={loading} error={error} minHeight={340} loadingHeight={260}>
    {data && (
      <BarChart
        height={300}
        xAxis={[{ scaleType: 'band', data: data.map((m) => m.metric), categoryGapRatio: 0.3 }]}
        yAxis={[{ min: 0 }]}
        series={data.map((item, idx) => ({
          label: item.metric,
          color: colors[idx % colors.length],
          stack: 'total',
          data: data.map((_, dataIdx) => (dataIdx === idx ? item.value : 0)),
        }))}
        margin={{ top: 25, right: 20, bottom: 35, left: 20 }}
        slotProps={{ tooltip: { trigger: 'item' } }}
        sx={{
          '& .MuiChartsAxis-tickLabel': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
          '& .MuiChartsAxis-label': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
          '& .MuiChartsLegend-label': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
          '& .MuiChartsTooltip-paper': {
            backgroundColor: (theme: Theme) => theme.palette.background.paper,
            border: (theme: Theme) => `1px solid ${theme.palette.divider}`,
          },
          '& .MuiChartsTooltip-root *': {
            color: (theme: Theme) => `${theme.palette.text.primary} !important`,
          },
        }}
      />
    )}
  </LargeTile>
);

export default ProcessingMetricsTile;
