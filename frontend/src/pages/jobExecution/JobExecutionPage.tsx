import { Box, Container, Grid, Link as MuiLink } from '@mui/material';
import { PageBreadcrumb } from '~/components';
import { humanize } from '~/utils';
import {
  DurationTile,
  ExecutionStatusTile,
  ReadWriteTile,
  StepDurationsTile,
  StepsTableTile,
  StepsTile,
} from './components';

export type { StepRow, StepStatus } from './types';

type Props = {
  jobId: string | undefined;
  executionId: string | undefined;
  environment: string;
  onJobClick: () => void;
};

const JobExecutionPage = ({
  jobId,
  executionId,
  environment,
  onJobClick,
}: Props) => (
  <Container
    component="main"
    maxWidth="xl"
    sx={{ py: 4, color: 'text.primary' }}
  >
    <PageBreadcrumb
      segments={[
        { label: environment },
        { label: jobId ?? 'Job', onClick: onJobClick },
        { label: `Execution #${executionId ?? '—'}` },
      ]}
    />

    <Grid container spacing={2.5}>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <StepsTile executionId={executionId} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <ReadWriteTile executionId={executionId} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <DurationTile executionId={executionId} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <ExecutionStatusTile executionId={executionId} />
      </Grid>

      <Grid size={{ xs: 12 }}>
        <StepDurationsTile executionId={executionId} />
      </Grid>

      <Grid size={{ xs: 12 }}>
        <StepsTableTile executionId={executionId} />
      </Grid>

      <Grid size={{ xs: 12 }}>
        <Box sx={{ mt: 1 }}>
          <MuiLink
            component="button"
            onClick={onJobClick}
            sx={{
              color: 'primary.dark',
              fontWeight: 700,
              textDecoration: 'none',
              '&:hover': { textDecoration: 'underline' },
            }}
          >
            ← Back to {humanize(jobId)}
          </MuiLink>
        </Box>
      </Grid>
    </Grid>
  </Container>
);

export default JobExecutionPage;
