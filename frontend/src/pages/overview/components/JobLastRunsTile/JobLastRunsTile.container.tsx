import { useNavigate } from 'react-router-dom';
import { getJobLastRuns } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import { useChartedJobs } from '../../ChartedJobsContext';
import JobLastRunsTile from './JobLastRunsTile';

const JobLastRunsTileContainer = () => {
  const navigate = useNavigate();
  const { windowDays } = useWindow();
  const state = useEnvQuery(['job-last-runs', windowDays], () => getJobLastRuns(windowDays));
  const { chartableJobs, chartedJobs, colorByJob, toggleChartedJob } = useChartedJobs();

  return (
    <JobLastRunsTile
      {...state}
      chartableJobs={chartableJobs}
      chartedJobs={chartedJobs}
      colorByJob={colorByJob}
      onJobClick={(jobName) => navigate(`/jobs/${encodeURIComponent(jobName)}`)}
      onRunClick={(jobName, executionId) =>
        navigate(`/jobs/${encodeURIComponent(jobName)}/executions/${executionId}`)
      }
      onToggleChart={toggleChartedJob}
    />
  );
};

export default JobLastRunsTileContainer;
