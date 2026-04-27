import { useNavigate, useParams } from 'react-router-dom';
import { getLastRun } from '~/api';
import { useJobQuery } from '~/hooks';
import LastRunTile from './LastRunTile';

const LastRunTileContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const state = useJobQuery(['job-last-run'], getLastRun);
  return (
    <LastRunTile
      {...state}
      onClick={jobId ? (id) => navigate(`/jobs/${jobId}/executions/${id}`) : null}
    />
  );
};

export default LastRunTileContainer;
