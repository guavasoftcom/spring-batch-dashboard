import { getRuntime } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import RuntimeTile from './RuntimeTile';

const RuntimeTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['runtime', windowDays], () => getRuntime(windowDays));
  return <RuntimeTile {...state} />;
};

export default RuntimeTileContainer;
