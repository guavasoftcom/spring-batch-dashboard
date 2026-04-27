import { StatTile } from '~/components';
import type { IoSummary } from '~/pages/jobExecution/types';

type Props = {
  data: IoSummary | null;
  loading: boolean;
  error: string | null;
};

const ReadWriteTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Read / Write"
    value={data && `${data.totalRead.toLocaleString()} / ${data.totalWrite.toLocaleString()}`}
    subtitle={data && 'Across all steps'}
    loading={loading}
    error={error}
  />
);

export default ReadWriteTile;
