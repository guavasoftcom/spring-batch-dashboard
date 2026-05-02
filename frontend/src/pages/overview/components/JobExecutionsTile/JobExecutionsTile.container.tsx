import { getJobCounts } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import JobExecutionsTile from './JobExecutionsTile';

const JobExecutionsTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['job-counts', windowDays], () => getJobCounts(windowDays));
  return <JobExecutionsTile {...state} />;
};

export default JobExecutionsTileContainer;
