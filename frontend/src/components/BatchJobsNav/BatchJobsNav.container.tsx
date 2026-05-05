import { useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getJobs } from '~/api';
import { useEnvironment } from '~/shell/EnvironmentContext';
import BatchJobsNav from './BatchJobsNav';

type Props = {
  collapsed?: boolean;
};

const BatchJobsNavContainer = ({ collapsed = false }: Props) => {
  const navigate = useNavigate();
  const { jobId } = useParams<{ jobId?: string }>();
  const { environment } = useEnvironment();

  const { data, isPending } = useQuery({
    queryKey: ['jobs', environment],
    queryFn: getJobs,
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
