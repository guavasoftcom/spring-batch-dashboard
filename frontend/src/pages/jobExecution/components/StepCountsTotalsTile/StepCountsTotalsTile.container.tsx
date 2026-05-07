import { getStepCountsSummary } from '~/api';
import { useExecutionQuery } from '~/hooks';
import StepCountsTotalsTile from './StepCountsTotalsTile';

type Props = { executionId: string | undefined };

const StepCountsTotalsTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(
    executionId,
    ['job-execution-step-counts-summary'],
    getStepCountsSummary,
  );
  return <StepCountsTotalsTile {...state} />;
};

export default StepCountsTotalsTileContainer;
