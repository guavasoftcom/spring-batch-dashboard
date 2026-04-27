import { getThroughput } from '~/api';
import { useEnvQuery } from '~/hooks';
import ThroughputTile from './ThroughputTile';

const ThroughputTileContainer = () => {
  const state = useEnvQuery(['throughput'], getThroughput);
  return <ThroughputTile {...state} />;
};

export default ThroughputTileContainer;
