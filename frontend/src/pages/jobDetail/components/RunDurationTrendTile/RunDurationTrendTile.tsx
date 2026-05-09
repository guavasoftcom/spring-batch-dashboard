import { useMemo } from 'react';
import { LineChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import { appColors } from '~/theme';
import type { RunStatus } from '~/types';
import { createClickableTickLabel } from './ClickableTickLabel';
import { createRunStatusTooltip } from './RunStatusTooltip';

export type RunDurationTrendChartData = {
  runs: { executionId: number; status: RunStatus }[];
  durations: number[];
  reads: number[];
  writes: number[];
};

type Props = {
  loading: boolean;
  error: string | null;
  chartData: RunDurationTrendChartData | null;
  /** When set, the chart's x-axis tick labels become clickable and call this with the picked execution id. */
  onTickClick?: (executionId: number) => void;
  /** When set, clicks on the chart axis area trigger this with MUI x-charts' axis-click payload. */
  onAxisClick?: (event: unknown, axisData: { dataIndex?: number | null } | null) => void;
};

const RunDurationTrendTile = ({ loading, error, chartData, onTickClick, onAxisClick }: Props) => {
  // Memoize slot components so their identity is stable across renders. Without this,
  // every parent re-render hands MUI x-charts a fresh tooltip component, which it
  // unmounts/remounts mid-hover — the brief remount renders at the default (0,0)
  // position before pointer-anchoring kicks in, producing the top-left flicker.
  const tooltipSlot = useMemo(
    () => (chartData ? createRunStatusTooltip(chartData.runs) : undefined),
    [chartData],
  );
  const tickLabelSlot = useMemo(
    () => (onTickClick ? createClickableTickLabel(onTickClick) : undefined),
    [onTickClick],
  );
  const chartSlots = useMemo(
    () => ({
      ...(tooltipSlot && { tooltip: tooltipSlot }),
      ...(tickLabelSlot && { axisTickLabel: tickLabelSlot }),
    }),
    [tooltipSlot, tickLabelSlot],
  );

  const xAxis = useMemo(
    () =>
      chartData
        ? [{
            scaleType: 'point' as const,
            data: chartData.runs.map((r) => `#${r.executionId}`),
          }]
        : undefined,
    [chartData],
  );
  const yAxis = useMemo(
    () => [
      { id: 'duration', min: 0, label: 'Seconds' },
      { id: 'records', min: 0, label: 'Records', position: 'right' as const },
    ],
    [],
  );
  const series = useMemo(() => {
    if (!chartData) {
      return [];
    }
    return [
      {
        id: 'duration' as const,
        label: 'Duration',
        data: chartData.durations,
        color: appColors.brandBlueLight,
        yAxisId: 'duration',
        area: true,
      },
      {
        id: 'read' as const,
        label: 'Read',
        data: chartData.reads,
        color: '#66BB6A',
        yAxisId: 'records',
      },
      {
        id: 'write' as const,
        label: 'Write',
        data: chartData.writes,
        color: '#FFA726',
        yAxisId: 'records',
      },
    ];
  }, [chartData]);
  const margin = useMemo(() => ({ top: 10, right: 30, bottom: 10, left: 10 }), []);
  const chartSx = useMemo(
    () => ({ '& .execution-tick-label:hover text': { textDecoration: 'underline' } }),
    [],
  );

  return (
    <LargeTile title="Run Duration Trend" loading={loading} error={error}>
      {chartData && xAxis && (
        <LineChart
          height={220}
          xAxis={xAxis}
          yAxis={yAxis}
          series={series}
          margin={margin}
          slots={chartSlots}
          onAxisClick={onAxisClick}
          sx={chartSx}
        />
      )}
    </LargeTile>
  );
};

export default RunDurationTrendTile;
