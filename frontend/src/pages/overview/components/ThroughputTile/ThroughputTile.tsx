import { StatTile } from '~/components';
import type { ThroughputSummary } from '~/types';

type Props = { data: ThroughputSummary | null; loading: boolean; error: string | null };

const ThroughputTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Throughput"
    value={data && `${data.readCount.toLocaleString()} / ${data.writeCount.toLocaleString()}`}
    subtitle={data && 'Read / Write records'}
    loading={loading}
    error={error}
    loadingValueWidth={140}
    loadingSubtitleWidth="60%"
  />
);

export default ThroughputTile;
