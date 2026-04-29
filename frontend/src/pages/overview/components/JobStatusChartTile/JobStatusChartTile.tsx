import { BarChart } from '@mui/x-charts';
import type { Theme } from '@mui/material/styles';
import { LargeTile } from '~/components';
import { appColors } from '~/theme';
import type { JobStatusSlice } from '~/types';

type Props = {
  data: JobStatusSlice[] | null;
  loading: boolean;
  error: string | null;
};

const JobStatusChartTile = ({ data, loading, error }: Props) => (
  <LargeTile title="Job Status Distribution" loading={loading} error={error} minHeight={340}>
    {data && (
      <BarChart
        xAxis={[{
          scaleType: 'band',
          data: data.map((s) => s.label),
          tickLabelStyle: { fontSize: 12 },
          tickLabelInterval: () => true,
        }]}
        yAxis={[{ min: 0, label: 'Runs' }]}
        series={data.map((s) => ({
          data: data.map((d) => (d.id === s.id ? d.value : 0)),
          label: s.label,
          color: s.color,
          stack: 'total',
        }))}
        height={300}
        margin={{ top: 20, bottom: 40, left: 20, right: 20 }}
        slotProps={{ tooltip: { trigger: 'item' } }}
        sx={{
          '& .MuiChartsLegend-label': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
          '& .MuiChartsAxis-tickLabel': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
          '& .MuiChartsAxis-label': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
          '& .MuiChartsTooltip-root *': { color: '#1A2733 !important' },
          '& .MuiChartsTooltip-paper': { backgroundColor: appColors.white, border: '1px solid #D5DBE3' },
        }}
      />
    )}
  </LargeTile>
);

export default JobStatusChartTile;
