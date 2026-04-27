import { useNavigate, useParams } from 'react-router-dom';
import { useEnvironment } from '~/shell/EnvironmentContext';
import JobExecutionPage from './JobExecutionPage';

const JobExecutionPageContainer = () => {
  const navigate = useNavigate();
  const { jobId, executionId } = useParams<{ jobId: string; executionId: string }>();
  const { environment } = useEnvironment();

  return (
    <JobExecutionPage
      jobId={jobId}
      executionId={executionId}
      environment={environment}
      onJobClick={() => navigate(`/jobs/${jobId}`)}
    />
  );
};

export default JobExecutionPageContainer;
