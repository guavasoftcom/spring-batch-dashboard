import { Box, Container, Grid } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import { PageBreadcrumb, WindowSelect } from '~/components';
import {
  JobExecutionsTile,
  JobLastRunsTile,
  JobStatusChartTile,
  ProcessingMetricsTile,
  RuntimeTile,
  StepExecutionsTile,
  ThroughputTile,
} from './components';

const OverviewPage = () => (
  <Container component="main" maxWidth="xl" sx={{ py: 4, color: 'text.primary' }}>
    <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2, mb: 2 }}>
      <PageBreadcrumb
        segments={[
          { label: 'Overview', icon: <HomeIcon sx={{ fontSize: 26 }} /> },
        ]}
      />
      <WindowSelect />
    </Box>
    <Grid container spacing={2.5}>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><JobExecutionsTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><StepExecutionsTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><ThroughputTile /></Grid>
      <Grid size={{ xs: 12, sm: 6, lg: 3 }}><RuntimeTile /></Grid>
      <Grid size={{ xs: 12, md: 5 }}><JobStatusChartTile /></Grid>
      <Grid size={{ xs: 12, md: 7 }}><ProcessingMetricsTile /></Grid>
      <Grid size={{ xs: 12 }}><JobLastRunsTile /></Grid>
    </Grid>
  </Container>
);

export default OverviewPage;
