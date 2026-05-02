import type { Theme } from '@mui/material/styles';
import { LineChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import { appColors } from '~/theme';
import type { JobRun } from '~/types';

type Props = {
  data: JobRun[] | null;
  loading: boolean;
  error: string | null;
};

const RunDurationTrendTile = ({ data, loading, error }: Props) => (
  <LargeTile title="Run Duration Trend" loading={loading} error={error}>
    {data && (
      <LineChart
        height={220}
        xAxis={[{ scaleType: 'point', data: data.map((r) => `#${r.executionId}`) }]}
        yAxis={[
          { id: 'duration', min: 0, label: 'Seconds' },
          { id: 'records', min: 0, label: 'Records', position: 'right' },
        ]}
        series={[
          { label: 'Duration', data: data.map((r) => r.durationSeconds), color: appColors.brandBlueLight, yAxisId: 'duration', showMark: true },
          { label: 'Read', data: data.map((r) => r.readCount), color: '#66BB6A', yAxisId: 'records', showMark: true },
          { label: 'Write', data: data.map((r) => r.writeCount), color: '#FFA726', yAxisId: 'records', showMark: true },
        ]}
        margin={{ top: 20, right: 70, bottom: 30, left: 60 }}
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

export default RunDurationTrendTile;
