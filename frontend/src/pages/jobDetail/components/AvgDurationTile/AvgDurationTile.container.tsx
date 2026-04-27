import { getAvgDuration } from '~/api';
import { useJobQuery } from '~/hooks';
import AvgDurationTile from './AvgDurationTile';

const AvgDurationTileContainer = () => {
  const state = useJobQuery(['job-avg-duration'], getAvgDuration);
  return <AvgDurationTile {...state} />;
};

export default AvgDurationTileContainer;
