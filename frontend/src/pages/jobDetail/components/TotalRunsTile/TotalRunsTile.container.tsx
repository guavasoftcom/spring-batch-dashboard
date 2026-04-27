import { getRunCounts } from '~/api';
import { useJobQuery } from '~/hooks';
import TotalRunsTile from './TotalRunsTile';

const TotalRunsTileContainer = () => {
  const state = useJobQuery(['job-run-counts'], getRunCounts);
  return <TotalRunsTile {...state} />;
};

export default TotalRunsTileContainer;
