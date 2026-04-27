import { getStepCounts } from '~/api';
import { useEnvQuery } from '~/hooks';
import StepExecutionsTile from './StepExecutionsTile';

const StepExecutionsTileContainer = () => {
  const state = useEnvQuery(['step-counts'], getStepCounts);
  return <StepExecutionsTile {...state} />;
};

export default StepExecutionsTileContainer;
