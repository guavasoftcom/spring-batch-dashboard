import { useState, type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  AppBar,
  Avatar,
  Box,
  Button,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import { BatchJobsNav } from '~/components/BatchJobsNav';
import { ColorModeToggle } from '~/components/ColorModeToggle';
import { EnvironmentSelector } from '~/components/EnvironmentSelector';
import { useLocation, useNavigate } from 'react-router-dom';
import { getCurrentUser, logout } from '~/api';
import type { CurrentUserResponse } from '~/types';
import SpringLeafIcon from '~/pages/login/components/SpringLeafIcon';
import { appColors, pageGradient, useColorMode } from '~/theme';
import { EnvironmentContext } from './EnvironmentContext';

const ENV_STORAGE_KEY = 'spring-batch-dashboard.environment';

type AppShellProps = {
  children: ReactNode;
};

const initialsFor = (user: CurrentUserResponse | null): string => {
  if (!user) {return '?';}
  const source = user.name?.trim() || user.login || '';
  if (!source) {return '?';}
  const parts = source.split(/\s+/).filter(Boolean);
  if (parts.length === 1) {return parts[0].slice(0, 2).toUpperCase();}
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
};

const AppShell = ({ children }: AppShellProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { mode } = useColorMode();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [environment, setEnvironmentState] = useState<string>(() => {
    const stored = typeof window !== 'undefined' ? window.localStorage.getItem(ENV_STORAGE_KEY) : null;
    return stored ?? '';
  });
  const setEnvironment = (value: string) => {
    setEnvironmentState(value);
    window.localStorage.setItem(ENV_STORAGE_KEY, value);
  };
  const { data: user = null } = useQuery<CurrentUserResponse | null>({
    queryKey: ['current-user'],
    queryFn: () => getCurrentUser().catch(() => null),
    staleTime: 5 * 60_000,
  });

  const displayName = user?.name?.trim() || user?.login || '';

  const onDashboard = location.pathname === '/overview';

  const handleLogout = async () => {
    try {
      setIsLoggingOut(true);
      await logout();
    } finally {
      navigate('/', { replace: true });
      setIsLoggingOut(false);
    }
  };

  return (
    <EnvironmentContext.Provider value={{ environment, setEnvironment }}>
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', background: pageGradient[mode] }}>
      <AppBar
        position="static"
        sx={{
          background: 'transparent',
          color: 'text.primary',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Toolbar sx={{ minHeight: 72 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.1, flexGrow: 1 }}>
            <SpringLeafIcon sx={{ color: appColors.leafGreen, fontSize: 28, flexShrink: 0 }} />
            <Typography
              component="h1"
              sx={{
                color: 'text.primary',
                lineHeight: 1.1,
                fontSize: { xs: '1.2rem', sm: '1.45rem' },
                whiteSpace: 'nowrap',
                cursor: 'pointer',
              }}
              onClick={() => navigate('/overview')}
            >
              <Box component="span" sx={{ fontFamily: '"Trebuchet MS", "Segoe UI", sans-serif', fontWeight: 700 }}>
                Spring Batch
              </Box>
              <Box component="span" sx={{ ml: 1, fontFamily: '"Arial Black", "Segoe UI", sans-serif', fontWeight: 800 }}>
                Dashboard
              </Box>
            </Typography>
          </Box>
          {user && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mr: 2 }}>
              <Avatar
                src={user.avatarUrl ?? undefined}
                alt={displayName}
                sx={{
                  width: 36,
                  height: 36,
                  bgcolor: 'primary.dark',
                  color: appColors.white,
                  fontSize: 14,
                  fontWeight: 700,
                  border: 1,
                  borderColor: 'divider',
                }}
              >
                {initialsFor(user)}
              </Avatar>
              <Typography sx={{ color: 'text.primary', fontWeight: 600, fontSize: 14 }}>
                {displayName}
              </Typography>
            </Box>
          )}
          <ColorModeToggle sx={{ mr: 1 }} />
          <Button
            color="inherit"
            onClick={handleLogout}
            disabled={isLoggingOut}
            sx={{
              fontWeight: 700,
              textTransform: 'none',
              bgcolor: appColors.brandOrange,
              color: appColors.white,
              '&:hover': { bgcolor: appColors.brandOrangeDark },
              '&.Mui-disabled': { color: appColors.white, opacity: 0.75 },
            }}
          >
            Logout
          </Button>
        </Toolbar>
      </AppBar>

      <Box sx={{ flex: 1, display: 'flex', minHeight: 0 }}>
        <Box
          component="nav"
          sx={{
            width: 240,
            flexShrink: 0,
            bgcolor: 'background.paper',
            borderRight: 1,
            borderColor: 'divider',
            py: 2,
          }}
        >
          <EnvironmentSelector />
          <List dense>
            <ListItemButton
              selected={onDashboard}
              onClick={() => navigate('/overview')}
              sx={{
                mx: 1,
                borderRadius: 1,
                '&.Mui-selected': {
                  bgcolor: 'rgba(21, 101, 192, 0.12)',
                  color: 'primary.dark',
                },
                '&.Mui-selected:hover': { bgcolor: 'rgba(21, 101, 192, 0.18)' },
              }}
            >
              <ListItemText
                primary="Overview"
                slotProps={{
                  primary: { sx: { fontWeight: onDashboard ? 700 : 500, fontSize: 14 } },
                }}
              />
            </ListItemButton>
          </List>
          <BatchJobsNav />
        </Box>

        <Box sx={{ flex: 1, bgcolor: 'background.default', overflow: 'auto' }}>
          {children}
        </Box>
      </Box>
    </Box>
    </EnvironmentContext.Provider>
  );
};

export default AppShell;
