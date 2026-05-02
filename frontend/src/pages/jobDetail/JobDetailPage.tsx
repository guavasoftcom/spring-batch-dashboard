import { Container, Grid } from '@mui/material';
import SourceIcon from '@mui/icons-material/Source';
import { PageBreadcrumb } from '~/components';
import {
  AvgDurationTile,
  JobRunsTableTile,
  LastRunTile,
  RunDurationTrendTile,
  SuccessRateTile,
  TotalRunsTile,
} from './components';

type Props = { jobId: string | undefined };

const JobDetailPage = ({ jobId }: Props) => (
  <Container component="main" maxWidth="xl" sx={{ py: 4, color: 'text.primary' }}>
    <PageBreadcrumb
      segments={[
        { label: jobId ?? 'Job', icon: <SourceIcon sx={{ fontSize: 26 }} /> },
      ]}
    />

    <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><TotalRunsTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><SuccessRateTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><AvgDurationTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><LastRunTile /></Grid>
      <Grid size={{ xs: 12 }}><RunDurationTrendTile /></Grid>
    </Grid>

    <JobRunsTableTile />
  </Container>
);

export default JobDetailPage;
