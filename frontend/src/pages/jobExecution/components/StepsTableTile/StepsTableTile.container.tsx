import { keepPreviousData } from '@tanstack/react-query';
import { useState } from 'react';
import { getStepDetails } from '~/api';
import type { StepSortField } from '~/api/jobExecutionStepsApi';
import { useExecutionQuery, useTableState } from '~/hooks';
import { StepDetailModal } from '../StepDetailModal';
import StepsTableTile from './StepsTableTile';

const PAGE_SIZE = 10;

type Props = { executionId: string | undefined };

const StepsTableTileContainer = ({ executionId }: Props) => {
  const { sortBy, sortDir, page, setPage, onSortChange } = useTableState<StepSortField>('startTime');
  const [selectedStepId, setSelectedStepId] = useState<number | null>(null);

  const { data, loading, error } = useExecutionQuery(
    executionId,
    ['job-execution-steps', sortBy, sortDir, page, PAGE_SIZE],
    (id) => getStepDetails(id, sortBy, sortDir, page, PAGE_SIZE),
    { placeholderData: keepPreviousData },
  );

  return (
    <>
      <StepsTableTile
        data={data?.content ?? null}
        totalElements={data?.totalElements ?? 0}
        page={page}
        pageSize={PAGE_SIZE}
        onPageChange={setPage}
        loading={loading}
        error={error}
        sortBy={sortBy}
        sortDir={sortDir}
        onSortChange={onSortChange}
        onStepClick={setSelectedStepId}
      />
      <StepDetailModal
        open={selectedStepId !== null}
        executionId={executionId}
        stepExecutionId={selectedStepId}
        onClose={() => setSelectedStepId(null)}
      />
    </>
  );
};

export default StepsTableTileContainer;
