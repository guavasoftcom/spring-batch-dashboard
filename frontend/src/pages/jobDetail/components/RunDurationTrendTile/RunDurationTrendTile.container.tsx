import { getRunsTrend } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import RunDurationTrendTile from './RunDurationTrendTile';

const RunDurationTrendTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useJobQuery(
    ['job-runs-trend', windowDays],
    (jobId) => getRunsTrend(jobId, windowDays),
  );
  return <RunDurationTrendTile {...state} />;
};

export default RunDurationTrendTileContainer;
