import { StatTile } from '~/components';
import type { AvgDuration } from '~/types';

type Props = { data: AvgDuration | null; loading: boolean; error: string | null };

const AvgDurationTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Avg Duration"
    value={data && `${data.averageSeconds}s`}
    subtitle={data && 'Across finished runs'}
    loading={loading}
    error={error}
    loadingSubtitleWidth="70%"
  />
);

export default AvgDurationTile;
