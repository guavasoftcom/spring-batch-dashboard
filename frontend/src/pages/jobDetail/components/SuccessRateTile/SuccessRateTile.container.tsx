import { getSuccessRate } from '~/api';
import { useJobQuery } from '~/hooks';
import SuccessRateTile from './SuccessRateTile';

const SuccessRateTileContainer = () => {
  const state = useJobQuery(['job-success-rate'], getSuccessRate);
  return <SuccessRateTile {...state} />;
};

export default SuccessRateTileContainer;
