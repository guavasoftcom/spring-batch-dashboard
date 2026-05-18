import { useCallback, useMemo } from 'react';
import { keepPreviousData } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { getRunsTrend } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import RunDurationTrendTile from './RunDurationTrendTile';

const RunDurationTrendTileContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { windowDays } = useWindow();
  // keepPreviousData: while a new window value is fetching, keep showing the
  // previous data so the chart never unmounts. Without this, switching windows
  // before the new query is cached produces a brief data=undefined render that
  // unmounts the chart, then a remount when data arrives — visible as flicker.
  const { data, loading, error } = useJobQuery(
    ['job-runs-trend', windowDays],
    (id) => getRunsTrend(id, windowDays),
    { placeholderData: keepPreviousData },
  );

  const chartData = useMemo(() => {
    if (!data) {
      return null;
    }
    return {
      runs: data.map((r) => ({ executionId: r.executionId, status: r.status })),
      durations: data.map((r) => r.durationSeconds),
      reads: data.map((r) => r.readCount),
      writes: data.map((r) => r.writeCount),
    };
  }, [data]);

  const goToExecution = useCallback(
    (executionId: number) => {
      if (jobId) {
        navigate(`/jobs/${jobId}/executions/${executionId}`);
      }
    },
    [jobId, navigate],
  );

  const onAxisClick = useCallback(
    (_event: unknown, axisData: { dataIndex?: number | null } | null) => {
      if (!chartData) {
        return;
      }
      const dataIndex = axisData?.dataIndex;
      const run = dataIndex != null ? chartData.runs[dataIndex] : undefined;
      if (run) {
        goToExecution(run.executionId);
      }
    },
    [chartData, goToExecution],
  );

  return (
    <RunDurationTrendTile
      loading={loading}
      error={error}
      chartData={chartData}
      onTickClick={jobId ? goToExecution : undefined}
      onAxisClick={jobId ? onAxisClick : undefined}
    />
  );
};

export default RunDurationTrendTileContainer;
