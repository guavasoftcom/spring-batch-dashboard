import { getJobStatusChart } from '~/api';
import { useEnvQuery } from '~/hooks';
import JobStatusChartTile from './JobStatusChartTile';

const JobStatusChartTileContainer = () => {
  const state = useEnvQuery(['job-status-chart'], getJobStatusChart);
  return <JobStatusChartTile {...state} />;
};

export default JobStatusChartTileContainer;
