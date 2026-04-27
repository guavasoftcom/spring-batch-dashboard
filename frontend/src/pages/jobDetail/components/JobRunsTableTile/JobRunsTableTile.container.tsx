import { keepPreviousData } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { getRuns } from '~/api';
import type { RunSortField } from '~/api/jobRunsApi';
import { useJobQuery, useTableState } from '~/hooks';
import JobRunsTableTile from './JobRunsTableTile';

const PAGE_SIZE = 10;

const JobRunsTableTileContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { sortBy, sortDir, page, setPage, onSortChange } = useTableState<RunSortField>('executionId');

  const { data, loading, error } = useJobQuery(
    ['job-runs', sortBy, sortDir, page, PAGE_SIZE],
    (id) => getRuns(id, sortBy, sortDir, page, PAGE_SIZE),
    { placeholderData: keepPreviousData },
  );

  return (
    <JobRunsTableTile
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
      onRunClick={(id) => navigate(`/jobs/${jobId}/executions/${id}`)}
    />
  );
};

export default JobRunsTableTileContainer;
