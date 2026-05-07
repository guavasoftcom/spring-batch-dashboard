import { StatTile } from '~/components';
import type { DurationSummary } from '~/pages/jobExecution/types';
import { formatDuration } from '~/utils';

type Props = {
  data: DurationSummary | null;
  loading: boolean;
  error: string | null;
};

const DurationTile = ({ data, loading, error }: Props) => (
  <StatTile
    title="Duration"
    value={data && formatDuration(data.totalDurationSeconds)}
    subtitle={data && 'Total step runtime'}
    loading={loading}
    error={error}
  />
);

export default DurationTile;
