import { BarChart } from '@mui/x-charts';
import type { Theme } from '@mui/material/styles';
import { LargeTile } from '~/components';
import type { StepDuration } from '~/pages/jobExecution/types';
import { humanize } from '~/utils';

const stepColors = ['#42A5F5', '#66BB6A', '#FFA726', '#AB47BC', '#EF5350'];

type Props = {
  data: StepDuration[] | null;
  loading: boolean;
  error: string | null;
};

const StepDurationsTile = ({ data, loading, error }: Props) => (
  <LargeTile title="Step Durations" loading={loading} error={error}>
    {data && (
      <BarChart
        height={260}
        xAxis={[{ scaleType: 'band', data: data.map((s) => humanize(s.stepName)) }]}
        yAxis={[{ min: 0, label: 'Seconds' }]}
        series={data.map((s, idx) => ({
          label: humanize(s.stepName),
          color: stepColors[idx % stepColors.length],
          stack: 'total',
          data: data.map((_, dataIdx) => (dataIdx === idx ? s.durationSeconds : 0)),
        }))}
        margin={{ top: 20, right: 20, bottom: 40, left: 60 }}
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

export default StepDurationsTile;
