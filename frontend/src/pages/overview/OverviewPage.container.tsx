import { useEnvironment } from '~/shell/EnvironmentContext';
import OverviewPage from './OverviewPage';

const OverviewPageContainer = () => {
  const { environment } = useEnvironment();
  return <OverviewPage environment={environment} />;
};

export default OverviewPageContainer;
