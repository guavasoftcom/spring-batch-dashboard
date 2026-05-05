import { useNavigate, useParams } from 'react-router-dom';
import { getRunsTrend } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import RunDurationTrendTile from './RunDurationTrendTile';

const RunDurationTrendTileContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { windowDays } = useWindow();
  const state = useJobQuery(
    ['job-runs-trend', windowDays],
    (id) => getRunsTrend(id, windowDays),
  );
  return (
    <RunDurationTrendTile
      {...state}
      onRunClick={jobId ? (executionId) => navigate(`/jobs/${jobId}/executions/${executionId}`) : undefined}
    />
  );
};

export default RunDurationTrendTileContainer;
