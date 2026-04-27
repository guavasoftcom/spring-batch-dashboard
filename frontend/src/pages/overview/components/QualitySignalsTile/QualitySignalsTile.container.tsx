import { getQualitySignals } from '~/api';
import { useEnvQuery } from '~/hooks';
import QualitySignalsTile from './QualitySignalsTile';

const QualitySignalsTileContainer = () => {
  const state = useEnvQuery(['quality-signals'], getQualitySignals);
  return <QualitySignalsTile {...state} />;
};

export default QualitySignalsTileContainer;
