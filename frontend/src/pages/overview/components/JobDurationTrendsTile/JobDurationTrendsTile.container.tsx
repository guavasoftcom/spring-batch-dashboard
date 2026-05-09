import { getJobDurationTrends } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import JobDurationTrendsTile from './JobDurationTrendsTile';

const JobDurationTrendsTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['overview-job-duration-trends', windowDays], () => getJobDurationTrends(windowDays));
  return <JobDurationTrendsTile {...state} />;
};

export default JobDurationTrendsTileContainer;
