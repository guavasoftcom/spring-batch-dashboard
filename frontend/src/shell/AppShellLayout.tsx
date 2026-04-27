import { Outlet } from 'react-router-dom';
import AppShell from './AppShell';

const AppShellLayout = () => (
  <AppShell>
    <Outlet />
  </AppShell>
);

export default AppShellLayout;
