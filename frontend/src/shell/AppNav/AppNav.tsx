import { Box, List, ListItemButton, ListItemIcon, ListItemText, Tooltip } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import { BatchJobsNav } from '~/components/BatchJobsNav';
import { EnvironmentSelector } from '~/components/EnvironmentSelector';

type Props = {
  navOpen: boolean;
  isWide: boolean;
  collapsed: boolean;
  onDashboard: boolean;
  onBackdropClick: () => void;
  onNavigateOverview: () => void;
};

const AppNav = ({ navOpen, isWide, collapsed, onDashboard, onBackdropClick, onNavigateOverview }: Props) => {
  const overviewButton = (
    <ListItemButton
      selected={onDashboard}
      onClick={onNavigateOverview}
      aria-label="Overview"
      sx={{
        mx: 1,
        borderRadius: 1,
        justifyContent: collapsed ? 'center' : 'flex-start',
        '&.Mui-selected': {
          bgcolor: 'rgba(21, 101, 192, 0.12)',
          color: 'primary.dark',
        },
        '&.Mui-selected:hover': { bgcolor: 'rgba(21, 101, 192, 0.18)' },
      }}
    >
      <ListItemIcon sx={{ minWidth: collapsed ? 0 : 32, color: 'inherit', justifyContent: 'center' }}>
        <HomeIcon fontSize="small" />
      </ListItemIcon>
      {!collapsed && (
        <ListItemText
          primary="Overview"
          slotProps={{
            primary: { sx: { fontWeight: onDashboard ? 700 : 500, fontSize: 14 } },
          }}
        />
      )}
    </ListItemButton>
  );

  return (
    <>
      {!isWide && navOpen && (
        <Box
          onClick={onBackdropClick}
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
          width: collapsed ? 64 : 240,
          flexShrink: 0,
          bgcolor: 'background.paper',
          borderRight: 1,
          borderColor: 'divider',
          pb: 2,
          pt: 2,
          overflowY: 'auto',
          overflowX: 'hidden',
          transition: 'width 200ms ease',
          ...(isWide
            ? {
                position: 'sticky',
                top: 64,
                height: 'calc(100vh - 64px)',
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
        <EnvironmentSelector compact={collapsed} />
        <List sx={{ mt: collapsed ? 2 : 0 }}>
          {collapsed ? (
            <Tooltip title="Overview" placement="right">{overviewButton}</Tooltip>
          ) : overviewButton}
        </List>
        <BatchJobsNav collapsed={collapsed} />
      </Box>
    </>
  );
};

export default AppNav;
