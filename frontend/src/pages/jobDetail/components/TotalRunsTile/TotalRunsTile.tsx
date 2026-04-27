import { StatTile } from '~/components';
import type { RunCounts } from '~/types';

type Props = { data: RunCounts | null; loading: boolean; error: string | null };

const TotalRunsTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Total Runs"
    value={data?.total}
    subtitle={data && `${data.completed} completed, ${data.failed} failed`}
    loading={loading}
    error={error}
  />
);

export default TotalRunsTile;
