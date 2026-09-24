import { keepPreviousData } from '@tanstack/react-query';
import { getJobDurationTrends } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';

/**
 * Shared by the page container (to derive the charted-job selection) and the trends
 * tile; both calls resolve to the same cached query.
 */
export const useJobDurationTrends = () => {
  const { windowDays } = useWindow();
  // keepPreviousData: while a new window value is fetching, keep showing the
  // previous data so the chart never unmounts. Without this, switching windows
  // before the new query is cached produces a brief data=undefined render that
  // unmounts the chart, then a remount when data arrives — visible as flicker.
  return useEnvQuery(
    ['overview-job-duration-trends', windowDays],
    () => getJobDurationTrends(windowDays),
    { placeholderData: keepPreviousData },
  );
};
