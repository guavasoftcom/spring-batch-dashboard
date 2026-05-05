import { getSuccessRate } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import SuccessRateTile from './SuccessRateTile';

const SuccessRateTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useJobQuery(
    ['job-success-rate', windowDays],
    (jobId) => getSuccessRate(jobId, windowDays),
  );
  return <SuccessRateTile {...state} />;
};

export default SuccessRateTileContainer;
