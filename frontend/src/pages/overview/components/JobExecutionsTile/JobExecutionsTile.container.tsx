import { getJobCounts } from '~/api';
import { useEnvQuery } from '~/hooks';
import JobExecutionsTile from './JobExecutionsTile';

const JobExecutionsTileContainer = () => {
  const state = useEnvQuery(['job-counts'], getJobCounts);
  return <JobExecutionsTile {...state} />;
};

export default JobExecutionsTileContainer;
