import { useNavigate, useParams } from 'react-router-dom';
import { getLastRun } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import LastRunTile from './LastRunTile';

const LastRunTileContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { windowDays } = useWindow();
  const state = useJobQuery(
    ['job-last-run', windowDays],
    (id) => getLastRun(id, windowDays),
  );
  return (
    <LastRunTile
      {...state}
      onClick={jobId ? (id) => navigate(`/jobs/${jobId}/executions/${id}`) : null}
    />
  );
};

export default LastRunTileContainer;
