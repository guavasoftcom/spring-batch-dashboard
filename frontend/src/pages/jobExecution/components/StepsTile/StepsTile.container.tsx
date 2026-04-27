import { getJobExecutionStepCounts } from '~/api';
import { useExecutionQuery } from '~/hooks';
import StepsTile from './StepsTile';

type Props = { executionId: string | undefined };

const StepsTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(
    executionId,
    ['job-execution-step-counts'],
    getJobExecutionStepCounts,
  );
  return <StepsTile {...state} />;
};

export default StepsTileContainer;
