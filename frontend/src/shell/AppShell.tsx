import { useEffect, useState, type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  AppBar,
  Avatar,
  Box,
  Button,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import HomeIcon from '@mui/icons-material/Home';
import { BatchJobsNav } from '~/components/BatchJobsNav';
import { ColorModeToggle } from '~/components/ColorModeToggle';
import { EnvironmentSelector } from '~/components/EnvironmentSelector';
import { useLocation, useNavigate } from 'react-router-dom';
import { getCurrentUser, logout } from '~/api';
import type { CurrentUserResponse } from '~/types';
import SpringLeafIcon from '~/pages/login/components/SpringLeafIcon';
import { appColors, pageGradient, pageGradientBottom, useColorMode } from '~/theme';
import { EnvironmentContext } from './EnvironmentContext';
import { NavContext } from './NavContext';

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
  const theme = useTheme();
  const isWide = useMediaQuery(theme.breakpoints.up('md'));
  const [navOpen, setNavOpen] = useState(true);
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

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
  }, [location.pathname]);

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
    <NavContext.Provider value={{ navOpen, setNavOpen: (v) => setNavOpen(v) }}>
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', background: pageGradient[mode] }}>
      <AppBar
        position="sticky"
        sx={{
          top: 0,
          zIndex: (theme) => theme.zIndex.appBar,
          background: mode === 'light' ? '#134B99' : pageGradientBottom.dark,
          color: appColors.white,
          boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            inset: 0,
            backgroundImage: `linear-gradient(180deg, rgba(255,255,255,0.06) 0%, rgba(0,0,0,0) 100%), url(/login-pattern-${mode}.png)`,
            backgroundRepeat: 'no-repeat, repeat',
            backgroundPosition: '0 0, center -50px',
            maskImage: 'linear-gradient(to right, black 0%, black 35%, transparent 60%)',
            WebkitMaskImage: 'linear-gradient(to right, black 0%, black 35%, transparent 60%)',
            opacity: mode === 'light' ? 0.45 : 0.18,
            pointerEvents: 'none',
          },
        }}
      >
        <Toolbar sx={{ minHeight: 64, position: 'relative' }}>
          <IconButton
            onClick={() => setNavOpen((v) => !v)}
            aria-label="toggle navigation"
            sx={{ color: appColors.white, mr: 1 }}
          >
            <MenuIcon />
          </IconButton>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.1, flexGrow: 1 }}>
            <SpringLeafIcon sx={{ color: appColors.leafGreen, fontSize: 28, flexShrink: 0 }} />
            <Typography
              component="h1"
              sx={{
                color: appColors.white,
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
              <Typography sx={{ color: appColors.white, fontWeight: 600, fontSize: 14 }}>
                {displayName}
              </Typography>
            </Box>
          )}
          <ColorModeToggle sx={{ mr: 2, color: appColors.white }} />
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

      <Box sx={{ flex: 1, display: 'flex', minHeight: 0, position: 'relative' }}>
        {!isWide && navOpen && (
          <Box
            onClick={() => setNavOpen(false)}
            sx={{
              position: 'fixed',
              inset: 0,
              top: 64,
              bgcolor: 'rgba(0,0,0,0.4)',
              zIndex: (t) => t.zIndex.drawer - 1,
            }}
          />
        )}
        <Box
          component="nav"
          sx={{
            width: 240,
            flexShrink: 0,
            bgcolor: 'background.paper',
            borderRight: 1,
            borderColor: 'divider',
            pb: 2,
            overflowY: 'auto',
            ...(isWide
              ? {
                  position: 'sticky',
                  top: 64,
                  height: 'calc(100vh - 64px)',
                  display: navOpen ? 'block' : 'none',
                }
              : {
                  position: 'fixed',
                  top: 64,
                  left: 0,
                  height: 'calc(100vh - 64px)',
                  zIndex: (t) => t.zIndex.drawer,
                  transform: navOpen ? 'translateX(0)' : 'translateX(-100%)',
                  transition: 'transform 200ms ease',
                  boxShadow: navOpen ? '4px 0 12px rgba(0,0,0,0.2)' : 'none',
                }),
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
              <ListItemIcon sx={{ minWidth: 32, color: 'inherit' }}>
                <HomeIcon fontSize="small" />
              </ListItemIcon>
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

        <Box sx={{ flex: 1, bgcolor: 'background.default', minWidth: 0 }}>
          {children}
        </Box>
      </Box>
    </Box>
    </NavContext.Provider>
    </EnvironmentContext.Provider>
  );
};

export default AppShell;
