import { useParams } from 'react-router-dom';
import { useEnvironment } from '~/shell/EnvironmentContext';
import JobDetailPage from './JobDetailPage';

const JobDetailPageContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const { environment } = useEnvironment();
  return <JobDetailPage jobId={jobId} environment={environment} />;
};

export default JobDetailPageContainer;
