import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';
import type { Theme } from '@mui/material/styles';
import { LineChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import { appColors } from '~/theme';
import type { JobRun } from '~/types';

export type TrendWindowOption = { value: number; label: string };

type Props = {
  data: JobRun[] | null;
  loading: boolean;
  error: string | null;
  windowDays: number;
  windowOptions: readonly TrendWindowOption[];
  onWindowChange: (value: number) => void;
};

const RunDurationTrendTile = ({ data, loading, error, windowDays, windowOptions, onWindowChange }: Props) => (
  <LargeTile
    title="Run Duration Trend"
    loading={loading}
    error={error}
    headerAction={
      <FormControl size="small" sx={{ minWidth: 160 }}>
        <InputLabel id="trend-window-label">Window</InputLabel>
        <Select
          labelId="trend-window-label"
          label="Window"
          value={windowDays}
          onChange={(e) => onWindowChange(Number(e.target.value))}
        >
          {windowOptions.map((opt) => (
            <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
          ))}
        </Select>
      </FormControl>
    }
  >
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
          '& .MuiChartsTooltip-paper': { backgroundColor: appColors.white, border: '1px solid #D5DBE3' },
        }}
      />
    )}
  </LargeTile>
);

export default RunDurationTrendTile;
