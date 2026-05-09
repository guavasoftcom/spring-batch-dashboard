import { Box, Container, Grid, Link as MuiLink } from '@mui/material';
import DisplaySettingsIcon from '@mui/icons-material/DisplaySettings';
import DynamicFormOutlinedIcon from '@mui/icons-material/DynamicFormOutlined';
import { PageBreadcrumb } from '~/components';
import { humanize } from '~/utils';
import {
  DurationTile,
  ExecutionStatusTile,
  ExecutionTimingTile,
  StepCountsTotalsTile,
  StepsTableTile,
  StepsTile,
} from './components';

export type { StepRow, StepStatus } from './types';

type Props = {
  jobId: string | undefined;
  executionId: string | undefined;
  onJobClick: () => void;
};

const JobExecutionPage = ({ jobId, executionId, onJobClick }: Props) => (
  <Container component="main" maxWidth="xl" sx={{ py: 4, color: 'text.primary' }}>
    <PageBreadcrumb
      segments={[
        {
          label: jobId ?? 'Job',
          onClick: onJobClick,
          icon: <DisplaySettingsIcon sx={{ fontSize: 26 }} />,
        },
        {
          label: `Execution #${executionId ?? '—'}`,
          icon: <DynamicFormOutlinedIcon sx={{ fontSize: 26 }} />,
        },
      ]}
    />

    <Grid container spacing={2.5}>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <StepsTile executionId={executionId} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <ExecutionTimingTile executionId={executionId} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <DurationTile executionId={executionId} />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
        <ExecutionStatusTile executionId={executionId} />
      </Grid>

      <Grid size={{ xs: 12 }}>
        <StepCountsTotalsTile executionId={executionId} />
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
