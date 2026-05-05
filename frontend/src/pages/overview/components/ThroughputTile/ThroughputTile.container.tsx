import { getThroughput } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import ThroughputTile from './ThroughputTile';

const ThroughputTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['throughput', windowDays], () => getThroughput(windowDays));
  return <ThroughputTile {...state} />;
};

export default ThroughputTileContainer;
