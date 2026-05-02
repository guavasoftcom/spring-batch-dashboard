import { useNavigate, useParams } from 'react-router-dom';
import JobExecutionPage from './JobExecutionPage';

const JobExecutionPageContainer = () => {
  const navigate = useNavigate();
  const { jobId, executionId } = useParams<{ jobId: string; executionId: string }>();

  return (
    <JobExecutionPage
      jobId={jobId}
      executionId={executionId}
      onJobClick={() => navigate(`/jobs/${jobId}`)}
    />
  );
};

export default JobExecutionPageContainer;
