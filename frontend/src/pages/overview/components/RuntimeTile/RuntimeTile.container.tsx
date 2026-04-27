import { getRuntime } from '~/api';
import { useEnvQuery } from '~/hooks';
import RuntimeTile from './RuntimeTile';

const RuntimeTileContainer = () => {
  const state = useEnvQuery(['runtime'], getRuntime);
  return <RuntimeTile {...state} />;
};

export default RuntimeTileContainer;
