import { useEffect, useState, type ReactNode } from 'react';
import { Box } from '@mui/material';
import { useLocation } from 'react-router-dom';
import { pageGradient, useColorMode } from '~/theme';
import { AppHeader } from './AppHeader';
import { AppNav } from './AppNav';
import { EnvironmentContext } from './EnvironmentContext';
import { NavContext } from './NavContext';
import { DEFAULT_WINDOW_DAYS, WINDOW_STORAGE_KEY, WindowContext } from './WindowContext';

const ENV_STORAGE_KEY = 'spring-batch-dashboard.environment';

type AppShellProps = {
  children: ReactNode;
};

const AppShell = ({ children }: AppShellProps) => {
  const location = useLocation();
  const { mode } = useColorMode();
  const [navOpen, setNavOpen] = useState(true);

  const [environment, setEnvironmentState] = useState<string>(() => {
    const stored = typeof window !== 'undefined' ? window.localStorage.getItem(ENV_STORAGE_KEY) : null;
    return stored ?? '';
  });

  const setEnvironment = (value: string) => {
    setEnvironmentState(value);
    window.localStorage.setItem(ENV_STORAGE_KEY, value);
  };

  const [windowDays, setWindowDaysState] = useState<number>(() => {
    const stored = typeof window !== 'undefined' ? window.localStorage.getItem(WINDOW_STORAGE_KEY) : null;
    const parsed = stored != null ? Number(stored) : Number.NaN;
    return Number.isFinite(parsed) ? parsed : DEFAULT_WINDOW_DAYS;
  });
  
  const setWindowDays = (value: number) => {
    setWindowDaysState(value);
    window.localStorage.setItem(WINDOW_STORAGE_KEY, String(value));
  };

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
  }, [location.pathname]);

  return (
    <EnvironmentContext.Provider value={{ environment, setEnvironment }}>
    <WindowContext.Provider value={{ windowDays, setWindowDays }}>
    <NavContext.Provider value={{ navOpen, setNavOpen: (v) => setNavOpen(v) }}>
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', background: pageGradient[mode] }}>
      <AppHeader />
      <Box sx={{ flex: 1, display: 'flex', minHeight: 0, position: 'relative' }}>
        <AppNav />
        <Box sx={{ flex: 1, bgcolor: 'background.default', minWidth: 0 }}>
          {children}
        </Box>
      </Box>
    </Box>
    </NavContext.Provider>
    </WindowContext.Provider>
    </EnvironmentContext.Provider>
  );
};

export default AppShell;
