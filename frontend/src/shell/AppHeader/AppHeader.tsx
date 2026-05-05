import { useState } from 'react';
import {
  AppBar,
  Avatar,
  Box,
  ButtonBase,
  IconButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Toolbar,
  Typography,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import { ColorModeToggle } from '~/components/ColorModeToggle';
import SpringLeafIcon from '~/pages/login/components/SpringLeafIcon';
import { appColors, pageGradientBottom } from '~/theme';
import type { CurrentUserResponse } from '~/types';

type Props = {
  user: CurrentUserResponse | null;
  displayName: string;
  mode: 'light' | 'dark';
  isLoggingOut: boolean;
  onToggleNav: () => void;
  onTitleClick: () => void;
  onLogout: () => void;
};

const initialsFor = (user: CurrentUserResponse | null): string => {
  if (!user) {return '?';}
  const source = user.name?.trim() || user.login || '';
  if (!source) {return '?';}
  const parts = source.split(/\s+/).filter(Boolean);
  if (parts.length === 1) {return parts[0].slice(0, 2).toUpperCase();}
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
};

const AppHeader = ({ user, displayName, mode, isLoggingOut, onToggleNav, onTitleClick, onLogout }: Props) => {
  const [accountAnchor, setAccountAnchor] = useState<HTMLElement | null>(null);
  const accountMenuOpen = Boolean(accountAnchor);
  const closeAccountMenu = () => setAccountAnchor(null);
  const handleLogoutClick = () => {
    closeAccountMenu();
    onLogout();
  };

  return (
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
          onClick={onToggleNav}
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
            onClick={onTitleClick}
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
          <ButtonBase
            onClick={(event) => setAccountAnchor(event.currentTarget)}
            aria-label="account menu"
            aria-haspopup="menu"
            aria-expanded={accountMenuOpen}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              mr: 2,
              px: 1,
              py: 0.5,
              borderRadius: 1,
              transition: 'background-color 150ms ease',
              '&:hover': { bgcolor: 'rgba(255,255,255,0.10)' },
              '&:focus-visible': { outline: `2px solid ${appColors.white}`, outlineOffset: 2 },
            }}
          >
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
          </ButtonBase>
        )}
        <Menu
          anchorEl={accountAnchor}
          open={accountMenuOpen}
          onClose={closeAccountMenu}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          transformOrigin={{ vertical: 'top', horizontal: 'right' }}
          slotProps={{ paper: { sx: { mt: 0.5, minWidth: 160 } } }}
        >
          <MenuItem onClick={handleLogoutClick} disabled={isLoggingOut}>
            <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
            <ListItemText>Logout</ListItemText>
          </MenuItem>
        </Menu>
        <ColorModeToggle sx={{ color: appColors.white }} />
      </Toolbar>
    </AppBar>
  );
};

export default AppHeader;
