import { getStepCounts } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import StepExecutionsTile from './StepExecutionsTile';

const StepExecutionsTileContainer = () => {
  const { windowDays } = useWindow();
  const state = useEnvQuery(['step-counts', windowDays], () => getStepCounts(windowDays));
  return <StepExecutionsTile {...state} />;
};

export default StepExecutionsTileContainer;
