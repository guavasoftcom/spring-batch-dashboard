import { useQuery } from '@tanstack/react-query';
import { getStepExecutionDetail } from '~/api';
import { useEnvironment } from '~/shell/EnvironmentContext';
import type { StepExecutionDetail } from '~/pages/jobExecution/types';
import StepDetailModal from './StepDetailModal';

type Props = {
  open: boolean;
  executionId: string | number | undefined;
  stepExecutionId: number | null;
  onClose: () => void;
};

const StepDetailModalContainer = ({ open, executionId, stepExecutionId, onClose }: Props) => {
  const { environment } = useEnvironment();
  const enabled = open && !!environment && executionId != null && stepExecutionId != null;

  const { data, isPending, error } = useQuery<StepExecutionDetail>({
    queryKey: ['step-execution-detail', environment, executionId, stepExecutionId],
    queryFn: () => getStepExecutionDetail(executionId!, stepExecutionId!),
    enabled,
  });

  return (
    <StepDetailModal
      open={open}
      data={enabled ? (data ?? null) : null}
      loading={enabled && isPending}
      error={error ? 'Failed to load step detail' : null}
      onClose={onClose}
    />
  );
};

export default StepDetailModalContainer;
