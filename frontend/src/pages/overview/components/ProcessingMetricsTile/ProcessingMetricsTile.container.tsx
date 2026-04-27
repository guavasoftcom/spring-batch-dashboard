import { getProcessingMetrics } from '~/api';
import { useEnvQuery } from '~/hooks';
import ProcessingMetricsTile from './ProcessingMetricsTile';

const ProcessingMetricsTileContainer = () => {
  const state = useEnvQuery(['processing-metrics'], getProcessingMetrics);
  return <ProcessingMetricsTile {...state} />;
};

export default ProcessingMetricsTileContainer;
