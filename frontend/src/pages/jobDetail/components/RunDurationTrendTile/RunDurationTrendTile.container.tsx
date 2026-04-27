import { useState } from 'react';
import { getRunsTrend } from '~/api';
import { useJobQuery } from '~/hooks';
import RunDurationTrendTile from './RunDurationTrendTile';

const trendWindowOptions = [
  { value: 7, label: 'Last 7 days' },
  { value: 30, label: 'Last 30 days' },
  { value: 60, label: 'Last 60 days' },
  { value: 90, label: 'Last 90 days' },
] as const;

const RunDurationTrendTileContainer = () => {
  const [windowDays, setWindowDays] = useState<number>(7);
  const state = useJobQuery(
    ['job-runs-trend', windowDays],
    (jobId) => getRunsTrend(jobId, windowDays),
  );
  return (
    <RunDurationTrendTile
      {...state}
      windowDays={windowDays}
      windowOptions={trendWindowOptions}
      onWindowChange={setWindowDays}
    />
  );
};

export default RunDurationTrendTileContainer;
