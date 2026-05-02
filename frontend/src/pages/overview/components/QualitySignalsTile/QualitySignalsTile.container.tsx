import { getQualitySignals } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import QualitySignalsTile from './QualitySignalsTile';

const QualitySignalsTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['quality-signals', windowDays], () => getQualitySignals(windowDays));
  return <QualitySignalsTile {...state} />;
};

export default QualitySignalsTileContainer;
