import { Box, Skeleton, Typography } from '@mui/material';
import { TilePaper } from '~/components';
import { appColors } from '~/theme';
import type { JobExecutionTiming } from '~/pages/jobExecution/types';
import { formatTimestamp } from '~/utils';

type Props = {
  data: JobExecutionTiming | null;
  loading: boolean;
  error: string | null;
};

const TimingRow = ({ label, value }: { label: string; value: string }) => (
  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 1 }}>
    <Typography
      component="span"
      sx={{ color: 'text.secondary', fontWeight: 700, fontSize: 11, textTransform: 'uppercase' }}
    >
      {label}
    </Typography>
    <Typography
      component="span"
      sx={{ color: 'primary.dark', fontFamily: 'monospace', fontSize: 13, fontWeight: 700 }}
    >
      {value}
    </Typography>
  </Box>
);

const ExecutionTimingTile = ({ data, loading, error }: Props) => (
  <TilePaper>
    <Typography variant="body2" sx={{ color: appColors.brandOrange, fontWeight: 700 }}>
      Timing
    </Typography>
    {loading && (
      <>
        <Skeleton variant="text" width="80%" sx={{ mt: 1 }} />
        <Skeleton variant="text" width="80%" />
        <Skeleton variant="text" width="80%" />
      </>
    )}
    {error && !loading && (
      <Typography color="error" variant="body2" sx={{ mt: 1 }}>{error}</Typography>
    )}
    {!loading && !error && data && (
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75, mt: 1.25 }}>
        <TimingRow label="Created" value={formatTimestamp(data.createTime)} />
        {data.startTime && <TimingRow label="Started" value={formatTimestamp(data.startTime)} />}
        {data.endTime && <TimingRow label="Ended" value={formatTimestamp(data.endTime)} />}
      </Box>
    )}
  </TilePaper>
);

export default ExecutionTimingTile;
