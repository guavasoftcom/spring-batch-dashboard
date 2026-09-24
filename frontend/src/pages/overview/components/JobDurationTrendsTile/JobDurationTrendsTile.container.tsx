import { useChartedJobs } from '../../ChartedJobsContext';
import { useJobDurationTrends } from '../../useJobDurationTrends';
import JobDurationTrendsTile from './JobDurationTrendsTile';

const JobDurationTrendsTileContainer = () => {
  const state = useJobDurationTrends();
  const { chartedJobs, colorByJob } = useChartedJobs();
  return <JobDurationTrendsTile {...state} chartedJobs={chartedJobs} colorByJob={colorByJob} />;
};

export default JobDurationTrendsTileContainer;
