import { getStepDurations } from '~/api';
import { useExecutionQuery } from '~/hooks';
import StepDurationsTile from './StepDurationsTile';

type Props = { executionId: string | undefined };

const StepDurationsTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(executionId, ['job-execution-step-durations'], getStepDurations);
  return <StepDurationsTile {...state} />;
};

export default StepDurationsTileContainer;
