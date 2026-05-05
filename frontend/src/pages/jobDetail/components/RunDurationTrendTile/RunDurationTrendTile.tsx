import { useState } from 'react';
import type { Theme } from '@mui/material/styles';
import { LineChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import { appColors } from '~/theme';
import type { JobRun } from '~/types';
import { createClickableTickLabel } from './ClickableTickLabel';
import { createRunStatusTooltip } from './RunStatusTooltip';
import SeriesToggles, { type SeriesId } from './SeriesToggles';

type Props = {
  data: JobRun[] | null;
  loading: boolean;
  error: string | null;
  /** When set, the chart's x-axis tick labels become clickable and call this with the picked execution id. */
  onRunClick?: (executionId: number) => void;
};

const RunDurationTrendTile = ({ data, loading, error, onRunClick }: Props) => {
  const [hiddenSeries, setHiddenSeries] = useState<Set<SeriesId>>(new Set());
  const toggleSeries = (id: SeriesId) => {
    setHiddenSeries((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  return (
    <LargeTile
      title="Run Duration Trend"
      loading={loading}
      error={error}
      headerAction={data ? <SeriesToggles hidden={hiddenSeries} onToggle={toggleSeries} /> : undefined}
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
            { id: 'duration', label: 'Duration', data: data.map((r) => r.durationSeconds), color: appColors.brandBlueLight, yAxisId: 'duration', showMark: true },
            { id: 'read',     label: 'Read',     data: data.map((r) => r.readCount),       color: '#66BB6A',                 yAxisId: 'records',  showMark: true },
            { id: 'write',    label: 'Write',    data: data.map((r) => r.writeCount),      color: '#FFA726',                 yAxisId: 'records',  showMark: true },
          ].filter((s) => !hiddenSeries.has(s.id as SeriesId))}
          hideLegend
          margin={{ top: 10, right: 30, bottom: 10, left: 10 }}
          slots={{
            tooltip: createRunStatusTooltip(data),
            ...(onRunClick && { axisTickLabel: createClickableTickLabel(onRunClick) }),
          }}
          onAxisClick={onRunClick && ((_event, axisData) => {
            const dataIndex = axisData?.dataIndex;
            if (dataIndex != null && data[dataIndex]) {
              onRunClick(data[dataIndex].executionId);
            }
          })}
          sx={{
            '& .MuiChartsAxis-tickLabel': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
            '& .MuiChartsAxis-label': { fill: (theme: Theme) => theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F' },
            '& .execution-tick-label:hover text': { textDecoration: 'underline' },
          }}
        />
      )}
    </LargeTile>
  );
};

export default RunDurationTrendTile;
