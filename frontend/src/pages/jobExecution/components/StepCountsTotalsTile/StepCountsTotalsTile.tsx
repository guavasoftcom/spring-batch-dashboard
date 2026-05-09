import { LargeTile, StepCountsBarChart } from '~/components';
import type { StepCountsSummary } from '~/types';

type Props = {
  data: StepCountsSummary | null;
  loading: boolean;
  error: string | null;
};

const StepCountsTotalsTile = ({ data, loading, error }: Props) => (
  <LargeTile
    title="Counts Across All Steps"
    loading={loading}
    error={error}
    minHeight={340}
    loadingHeight={260}
  >
    {data && <StepCountsBarChart data={data} />}
  </LargeTile>
);

export default StepCountsTotalsTile;
