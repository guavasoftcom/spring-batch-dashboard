import { StatTile } from '~/components';
import type { ExecutionCounts } from '~/types';

type Props = {
  data: ExecutionCounts | null;
  loading: boolean;
  error: string | null;
};

const StepExecutionsTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Step Executions"
    value={data?.total}
    subtitle={data && `${data.completed} completed, ${data.failed} failed, ${data.started} active`}
    loading={loading}
    error={error}
  />
);

export default StepExecutionsTile;
