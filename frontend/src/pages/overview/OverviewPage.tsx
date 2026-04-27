import { Container, Grid } from '@mui/material';
import { PageBreadcrumb } from '~/components';
import {
  JobExecutionsTile,
  JobStatusChartTile,
  ProcessingMetricsTile,
  QualitySignalsTile,
  RuntimeTile,
  StepExecutionsTile,
  ThroughputTile,
} from './components';

type Props = { environment: string };

const OverviewPage = ({ environment }: Props) => (
  <Container component="main" maxWidth="xl" sx={{ py: 4, color: 'text.primary' }}>
    <PageBreadcrumb segments={[{ label: environment }, { label: 'Overview' }]} />
    <Grid container spacing={2.5}>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><JobExecutionsTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StepExecutionsTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><ThroughputTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><RuntimeTile /></Grid>
      <Grid size={{ xs: 12, md: 5 }}><JobStatusChartTile /></Grid>
      <Grid size={{ xs: 12, md: 7 }}><ProcessingMetricsTile /></Grid>
      <Grid size={{ xs: 12 }}><QualitySignalsTile /></Grid>
    </Grid>
  </Container>
);

export default OverviewPage;
