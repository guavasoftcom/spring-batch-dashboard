import { Navigate, Route, Routes } from 'react-router-dom';
import {
  OverviewPage,
  JobDetailPage,
  JobExecutionPage,
  LoginPage,
} from './pages';
import AppShellLayout from '~/shell/AppShellLayout';

const App = () => {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route element={<AppShellLayout />}>
        <Route path="/overview" element={<OverviewPage />} />
        <Route path="/jobs/:jobId" element={<JobDetailPage />} />
        <Route path="/jobs/:jobId/executions/:executionId" element={<JobExecutionPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
