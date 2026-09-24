import { useCallback, useMemo, useState } from 'react';
import { useEnvironment } from '~/shell/EnvironmentContext';
import {
  ChartedJobsContext,
  MAX_CHARTED_JOBS,
  assignJobColors,
  defaultChartedJobs,
} from './ChartedJobsContext';
import OverviewPage from './OverviewPage';
import { useJobDurationTrends } from './useJobDurationTrends';

// The user's picks, tagged with the environment they were made in so switching
// environments falls back to the default selection instead of stale job names.
type UserSelection = { environment: string; jobs: string[] };

const OverviewPageContainer = () => {
  const { environment } = useEnvironment();
  const { data: trends } = useJobDurationTrends();
  const [userSelection, setUserSelection] = useState<UserSelection | null>(null);

  const chartableJobs = useMemo(() => (trends ?? []).map((series) => series.jobName), [trends]);

  const chartedJobs = useMemo(() => {
    if (!userSelection || userSelection.environment !== environment) {
      return defaultChartedJobs(trends ?? []);
    }
    // Keep list order (not click order) so chart legend and colours stay predictable.
    return chartableJobs.filter((jobName) => userSelection.jobs.includes(jobName));
  }, [userSelection, environment, trends, chartableJobs]);

  const colorByJob = useMemo(() => assignJobColors(chartableJobs, chartedJobs), [chartableJobs, chartedJobs]);

  const toggleChartedJob = useCallback(
    (jobName: string) => {
      if (chartedJobs.includes(jobName)) {
        setUserSelection({ environment, jobs: chartedJobs.filter((charted) => charted !== jobName) });
      } else if (chartedJobs.length < MAX_CHARTED_JOBS) {
        setUserSelection({ environment, jobs: [...chartedJobs, jobName] });
      }
    },
    [chartedJobs, environment],
  );

  const contextValue = useMemo(
    () => ({ chartableJobs, chartedJobs, colorByJob, toggleChartedJob }),
    [chartableJobs, chartedJobs, colorByJob, toggleChartedJob],
  );

  return (
    <ChartedJobsContext.Provider value={contextValue}>
      <OverviewPage />
    </ChartedJobsContext.Provider>
  );
};

export default OverviewPageContainer;
