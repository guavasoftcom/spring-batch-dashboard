import { getProcessingMetrics } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import ProcessingMetricsTile from './ProcessingMetricsTile';

const ProcessingMetricsTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['processing-metrics', windowDays], () => getProcessingMetrics(windowDays));
  return <ProcessingMetricsTile {...state} />;
};

export default ProcessingMetricsTileContainer;
