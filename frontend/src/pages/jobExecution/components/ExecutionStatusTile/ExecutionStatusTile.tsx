import { Chip } from '@mui/material';
import { StatTile } from '~/components';
import type { JobExecutionStepCounts } from '~/pages/jobExecution/types';

type Props = {
  data: JobExecutionStepCounts | null;
  loading: boolean;
  error: string | null;
};

type ExecutionStatus = 'COMPLETED' | 'FAILED' | 'STARTED' | 'NO STEPS';

const statusColor: Record<ExecutionStatus, 'success' | 'error' | 'info' | 'default'> = {
  COMPLETED: 'success',
  FAILED: 'error',
  STARTED: 'info',
  'NO STEPS': 'default',
};

const statusSubtitle: Record<ExecutionStatus, string> = {
  COMPLETED: 'All steps finished cleanly',
  FAILED: 'At least one step failed',
  STARTED: 'One or more steps still running',
  'NO STEPS': 'No steps recorded',
};

const deriveStatus = (counts: JobExecutionStepCounts): ExecutionStatus => {
  if (counts.totalSteps === 0) {
    return 'NO STEPS';
  }
  if (counts.failed > 0) {
    return 'FAILED';
  }
  if (counts.active > 0) {
    return 'STARTED';
  }
  return 'COMPLETED';
};

const ExecutionStatusTile = ({ data, loading, error }: Props) => {
  const status = data ? deriveStatus(data) : null;
  return (
    <StatTile
      title="Status"
      value={
        status ? (
          <Chip
            label={status}
            color={statusColor[status]}
            sx={{ fontWeight: 700, fontSize: 14, height: 32, px: 0.5 }}
          />
        ) : undefined
      }
      subtitle={status && statusSubtitle[status]}
      loading={loading}
      error={error}
    />
  );
};

export default ExecutionStatusTile;
