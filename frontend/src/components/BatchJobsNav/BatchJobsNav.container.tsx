import { useNavigate, useParams } from 'react-router-dom';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { getJobs } from '~/api';
import { useEnvironment } from '~/shell/EnvironmentContext';
import { useWindow } from '~/shell/WindowContext';
import BatchJobsNav from './BatchJobsNav';

type Props = {
  collapsed?: boolean;
};

const BatchJobsNavContainer = ({ collapsed = false }: Props) => {
  const navigate = useNavigate();
  const { jobId } = useParams<{ jobId?: string }>();
  const { environment } = useEnvironment();
  const { windowDays } = useWindow();

  // Only jobs with a run inside the selected window are listed. keepPreviousData holds the
  // current list while a new window loads, so the nav doesn't flash skeletons on every switch.
  const { data, isPending } = useQuery({
    queryKey: ['jobs', environment, windowDays],
    queryFn: () => getJobs(windowDays),
    placeholderData: keepPreviousData,
  });

  return (
    <BatchJobsNav
      jobs={data ?? []}
      activeJobId={jobId ?? null}
      loading={isPending}
      collapsed={collapsed}
      onSelect={(id) => navigate(`/jobs/${id}`)}
    />
  );
};

export default BatchJobsNavContainer;
