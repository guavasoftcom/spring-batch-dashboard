import { StatTile } from '~/components';
import type { SuccessRate } from '~/types';

type Props = { data: SuccessRate | null; loading: boolean; error: string | null };

const SuccessRateTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Success Rate"
    value={data && `${data.successRate}%`}
    subtitle={data && `${data.completed} of ${data.finished} finished runs`}
    loading={loading}
    error={error}
  />
);

export default SuccessRateTile;
