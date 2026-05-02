import { getAvgDuration } from '~/api';
import { useJobQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import AvgDurationTile from './AvgDurationTile';

const AvgDurationTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useJobQuery(
    ['job-avg-duration', windowDays],
    (jobId) => getAvgDuration(jobId, windowDays),
  );
  return <AvgDurationTile {...state} />;
};

export default AvgDurationTileContainer;
