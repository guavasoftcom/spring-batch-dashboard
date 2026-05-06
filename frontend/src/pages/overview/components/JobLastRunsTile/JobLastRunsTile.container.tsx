import { useNavigate } from 'react-router-dom';
import { getJobLastRuns } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import JobLastRunsTile from './JobLastRunsTile';

const JobLastRunsTileContainer = () => {
  const navigate = useNavigate();
  const { windowDays } = useWindow();
  const state = useEnvQuery(['job-last-runs', windowDays], () => getJobLastRuns(windowDays));

  return (
    <JobLastRunsTile
      {...state}
      onJobClick={(jobName) => navigate(`/jobs/${encodeURIComponent(jobName)}`)}
      onRunClick={(jobName, executionId) =>
        navigate(`/jobs/${encodeURIComponent(jobName)}/executions/${executionId}`)
      }
    />
  );
};

export default JobLastRunsTileContainer;
