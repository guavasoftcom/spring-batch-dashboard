import { StatTile } from '~/components';
import type { Durations } from '~/types';
import { formatDuration } from '~/utils';

type Props = { data: Durations | null; loading: boolean; error: string | null };

const RuntimeTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Runtime"
    value={data && formatDuration(data.averageSeconds)}
    subtitle={data && `Avg duration • Longest ${formatDuration(data.longestSeconds)}`}
    loading={loading}
    error={error}
    loadingSubtitleWidth="70%"
  />
);

export default RuntimeTile;
