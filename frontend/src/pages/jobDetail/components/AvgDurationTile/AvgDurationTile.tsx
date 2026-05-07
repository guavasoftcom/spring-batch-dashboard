import { StatTile } from '~/components';
import type { AvgDuration } from '~/types';
import { formatDuration } from '~/utils';

type Props = { data: AvgDuration | null; loading: boolean; error: string | null };

const AvgDurationTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Avg Duration"
    value={data && formatDuration(data.averageSeconds)}
    subtitle={data && 'Across finished runs'}
    loading={loading}
    error={error}
    loadingSubtitleWidth="70%"
  />
);

export default AvgDurationTile;
