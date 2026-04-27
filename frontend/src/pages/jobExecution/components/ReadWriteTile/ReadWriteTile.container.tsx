import { getIoSummary } from '~/api';
import { useExecutionQuery } from '~/hooks';
import ReadWriteTile from './ReadWriteTile';

type Props = { executionId: string | undefined };

const ReadWriteTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(executionId, ['job-execution-io'], getIoSummary);
  return <ReadWriteTile {...state} />;
};

export default ReadWriteTileContainer;
