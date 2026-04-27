import { getDurationSummary } from '~/api';
import { useExecutionQuery } from '~/hooks';
import DurationTile from './DurationTile';

type Props = { executionId: string | undefined };

const DurationTileContainer = ({ executionId }: Props) => {
  const state = useExecutionQuery(executionId, ['job-execution-duration'], getDurationSummary);
  return <DurationTile {...state} />;
};

export default DurationTileContainer;
