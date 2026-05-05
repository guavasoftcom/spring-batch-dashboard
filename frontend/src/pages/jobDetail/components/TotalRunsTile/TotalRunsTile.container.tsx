import { getRunCounts } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import TotalRunsTile from './TotalRunsTile';

const TotalRunsTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useJobQuery(
    ['job-run-counts', windowDays],
    (jobId) => getRunCounts(jobId, windowDays),
  );
  return <TotalRunsTile {...state} />;
};

export default TotalRunsTileContainer;
