import { BarChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import { appColors } from '~/theme';
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
          '& .MuiChartsAxis-tickLabel': { fill: '#37474F' },
          '& .MuiChartsAxis-label': { fill: '#37474F' },
          '& .MuiChartsLegend-label': { fill: '#37474F' },
          '& .MuiChartsTooltip-root *': { color: '#1A2733 !important' },
          '& .MuiChartsTooltip-paper': { backgroundColor: appColors.white, border: '1px solid #D5DBE3' },
        }}
      />
    )}
  </LargeTile>
);

export default ProcessingMetricsTile;
