import { getExecutionTiming } from '~/api';
import { useExecutionQuery } from '~/hooks';
import ExecutionTimingTile from './ExecutionTimingTile';

type Props = { executionId: string | undefined };

const ExecutionTimingTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(executionId, ['job-execution-timing'], getExecutionTiming);
  return <ExecutionTimingTile {...state} />;
};

export default ExecutionTimingTileContainer;
