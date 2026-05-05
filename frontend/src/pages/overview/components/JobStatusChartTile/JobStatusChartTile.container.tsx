import { getJobStatusChart } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import JobStatusChartTile from './JobStatusChartTile';

const JobStatusChartTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['job-status-chart', windowDays], () => getJobStatusChart(windowDays));
  return <JobStatusChartTile {...state} />;
};

export default JobStatusChartTileContainer;
