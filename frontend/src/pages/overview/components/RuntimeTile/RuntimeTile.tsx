import { StatTile } from '~/components';
import type { Durations } from '~/types';

type Props = { data: Durations | null; loading: boolean; error: string | null };

const RuntimeTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Runtime"
    value={data && `${data.averageSeconds}s`}
    subtitle={data && `Avg duration • Longest ${data.longestSeconds}s`}
    loading={loading}
    error={error}
    loadingSubtitleWidth="70%"
  />
);

export default RuntimeTile;
