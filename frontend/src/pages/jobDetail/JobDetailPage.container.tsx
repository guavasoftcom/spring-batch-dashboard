import { useParams } from 'react-router-dom';
import JobDetailPage from './JobDetailPage';

const JobDetailPageContainer = () => {
  const { jobId } = useParams<{ jobId: string }>();
  return <JobDetailPage jobId={jobId} />;
};

export default JobDetailPageContainer;
