import { Skeleton, Typography } from '@mui/material';
import { LargeTile } from '~/components';
import type { QualitySignals } from '~/types';
import { humanize } from '~/utils';

type Props = {
  data: QualitySignals | null;
  loading: boolean;
  error: string | null;
};

const loadingSkeleton = (
  <>
    <Skeleton variant="text" width="70%" sx={{ mt: 1 }} />
    <Skeleton variant="text" width="60%" sx={{ mt: 0.5 }} />
    <Skeleton variant="text" width="40%" sx={{ mt: 1 }} />
  </>
);

const QualitySignalsTile = ({ data, loading, error }: Props) => (
  <LargeTile
    title="Exception & Quality Signals"
    loading={loading}
    error={error}
    loadingSkeleton={loadingSkeleton}
  >
    {data && (
      <>
        <Typography variant="body1" sx={{ color: 'text.secondary', mt: 1 }}>
          Last failure: {humanize(data.lastFailure) ?? '—'}
        </Typography>
        <Typography variant="body1" sx={{ color: 'text.secondary', mt: 0.5 }}>
          Rollbacks: {data.processing.rollbackCount} • Skips:{' '}
          {data.processing.skipCount} • Filters: {data.processing.filterCount}
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.disabled', mt: 1 }}>
          Last updated: {data.latestUpdate ?? '—'}
        </Typography>
      </>
    )}
  </LargeTile>
);

export default QualitySignalsTile;
