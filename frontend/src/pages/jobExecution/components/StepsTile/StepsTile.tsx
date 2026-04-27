import { StatTile } from '~/components';
import type { JobExecutionStepCounts } from '~/pages/jobExecution/types';

type Props = {
  data: JobExecutionStepCounts | null;
  loading: boolean;
  error: string | null;
};

const StepsTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Steps"
    value={data?.totalSteps}
    subtitle={data && `${data.completed} completed, ${data.failed} failed, ${data.active} active`}
    loading={loading}
    error={error}
  />
);

export default StepsTile;
