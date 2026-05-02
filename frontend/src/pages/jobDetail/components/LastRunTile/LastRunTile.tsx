import { Skeleton, Typography } from '@mui/material';
import { appColors } from '~/theme';
import type { JobRun } from '~/types';
import { ExecutionLink, TilePaper } from '~/components';

type Props = {
  data: JobRun | null;
  loading: boolean;
  error: string | null;
  onClick: ((executionId: number) => void) | null;
};

const LastRunTile = ({ data, loading, error, onClick }: Props) => (
  <TilePaper>
    <Typography variant="body2" sx={{ color: appColors.brandOrange, fontWeight: 700 }}>
      Last Run
    </Typography>
    {loading && (
      <>
        <Skeleton variant="text" width={100} height={48} sx={{ mt: 1 }} />
        <Skeleton variant="text" width="80%" sx={{ mt: 1 }} />
      </>
    )}
    {error && <Typography color="error" variant="body2" sx={{ mt: 1 }}>{error}</Typography>}
    {data && (
      <>
        {onClick ? (
          <ExecutionLink executionId={data.executionId} onClick={onClick} variant="large" />
        ) : (
          <Typography variant="h4" sx={{ mt: 1, color: 'primary.dark', fontWeight: 800 }}>
            #{data.executionId}
          </Typography>
        )}
        <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
          {data.status} • {data.startTime}
        </Typography>
      </>
    )}
    {!loading && !error && !data && (
      <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>No runs yet</Typography>
    )}
  </TilePaper>
);

export default LastRunTile;
