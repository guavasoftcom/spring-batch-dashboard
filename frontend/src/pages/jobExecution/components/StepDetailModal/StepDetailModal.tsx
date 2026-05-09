import { Alert, Box, Dialog, DialogContent, Skeleton, Typography } from '@mui/material';
import { StatGrid, StepCountsBarChart, recordToStatEntries } from '~/components';
import type { StepExecutionDetail } from '~/pages/jobExecution/types';
import { formatTimestamp } from '~/utils';
import StepDetailModalHeader from './StepDetailModalHeader';

type Props = {
  open: boolean;
  data: StepExecutionDetail | null;
  loading: boolean;
  error: string | null;
  onClose: () => void;
};

const Section = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <Box sx={{ mb: 2 }}>
    <Typography variant="subtitle2" sx={{ color: 'primary.dark', fontWeight: 700, mb: 0.75 }}>
      {title}
    </Typography>
    {children}
  </Box>
);

const StepDetailModal = ({ open, data, loading, error, onClose }: Props) => (
  <Dialog
    open={open}
    onClose={onClose}
    maxWidth="lg"
    fullWidth
    scroll="paper"
    slotProps={{
      paper: {
        sx: (theme) => ({
          ...(theme.palette.mode === 'dark' && {
            // MUI Paper applies a `linear-gradient` overlay in dark mode to hint at
            // elevation, which tints the surface lighter than the backgroundColor.
            // Disable it so our chosen color actually renders.
            backgroundColor: '#1B2230',
            backgroundImage: 'none',
          }),
        }),
      },
    }}
  >
    <StepDetailModalHeader data={data} onClose={onClose} />
    <DialogContent dividers>
      {loading && (
        <>
          <Skeleton variant="text" width="60%" />
          <Skeleton variant="text" width="40%" />
          <Skeleton variant="rectangular" height={120} sx={{ mt: 2, borderRadius: 1 }} />
        </>
      )}
      {error && <Alert severity="error">{error}</Alert>}
      {data && !loading && !error && (
        <>
          <Section title="Counts">
            <StepCountsBarChart data={data} />
          </Section>

          <Section title="Times">
            <StatGrid
              entries={[
                { label: 'Created', value: formatTimestamp(data.createTime) },
                { label: 'Started', value: formatTimestamp(data.startTime) },
                { label: 'Ended', value: formatTimestamp(data.endTime) },
                { label: 'Last updated', value: formatTimestamp(data.lastUpdated) },
              ]}
            />
          </Section>

          <Section title="Exit">
            <StatGrid
              entries={[
                { label: 'Exit code', value: data.exitCode },
                {
                  label: 'Exit message',
                  value: data.status === 'FAILED' && data.exitMessage ? (
                    <Box component="span" sx={{ color: 'error.main' }}>{data.exitMessage}</Box>
                  ) : data.exitMessage,
                  colSpan: 3,
                },
              ]}
            />
          </Section>

          <Section title="Execution Context">
            {Object.keys(data.executionContext).length === 0 ? (
              <Typography variant="body2" sx={{ color: 'text.disabled' }}>
                No execution context.
              </Typography>
            ) : (
              <StatGrid entries={recordToStatEntries(data.executionContext)} />
            )}
          </Section>
        </>
      )}
    </DialogContent>
  </Dialog>
);

export default StepDetailModal;
