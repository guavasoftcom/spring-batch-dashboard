import { getJobExecutionStepCounts } from '~/api';
import { useExecutionQuery } from '~/hooks';
import ExecutionStatusTile from './ExecutionStatusTile';

type Props = { executionId: string | undefined };

const ExecutionStatusTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(
    executionId,
    ['job-execution-step-counts'],
    getJobExecutionStepCounts,
  );
  return <ExecutionStatusTile {...state} />;
};

export default ExecutionStatusTileContainer;
