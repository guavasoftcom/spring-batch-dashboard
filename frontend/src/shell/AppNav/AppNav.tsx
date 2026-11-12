import { Box, List, ListItemButton, ListItemIcon, ListItemText, Tooltip } from '@mui/material';
import AutoGraphOutlinedIcon from '@mui/icons-material/AutoGraphOutlined';
import { BatchJobsNav } from '~/components/BatchJobsNav';
import { EnvironmentSelector } from '~/components/EnvironmentSelector';
import { NAV_WIDTH_DEFAULT, NAV_WIDTH_MAX, NAV_WIDTH_MIN, type NavResizeHandleProps } from './useNavResize';

const NAV_WIDTH_COLLAPSED = 64;

type Props = {
  navOpen: boolean;
  isWide: boolean;
  collapsed: boolean;
  width: number;
  resizing: boolean;
  resizeHandleProps: NavResizeHandleProps;
  onDashboard: boolean;
  onBackdropClick: () => void;
  onNavigateOverview: () => void;
};

const AppNav = ({
  navOpen,
  isWide,
  collapsed,
  width,
  resizing,
  resizeHandleProps,
  onDashboard,
  onBackdropClick,
  onNavigateOverview,
}: Props) => {
  const overviewButton = (
    <Tooltip title="Overview" placement="right">
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
        <ListItemIcon
          sx={{ minWidth: collapsed ? 0 : 32, color: 'inherit', justifyContent: 'center' }}
        >
          <AutoGraphOutlinedIcon fontSize="small" />
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
    </Tooltip>
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
          width: collapsed ? NAV_WIDTH_COLLAPSED : isWide ? width : NAV_WIDTH_DEFAULT,
          flexShrink: 0,
          bgcolor: 'background.paper',
          borderRight: 1,
          borderColor: 'divider',
          // No width animation while dragging, or the nav lags behind the pointer.
          transition: resizing ? 'none' : 'width 200ms ease',
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
        <Box sx={{ height: '100%', py: 2, overflowY: 'auto', overflowX: 'hidden' }}>
          <EnvironmentSelector compact={collapsed} />
          <List sx={{ mt: collapsed ? 2 : 0 }}>
            {overviewButton}
          </List>
          <BatchJobsNav collapsed={collapsed} />
        </Box>
        {isWide && !collapsed && (
          <Box
            role="separator"
            aria-orientation="vertical"
            aria-label="Resize navigation"
            aria-valuenow={width}
            aria-valuemin={NAV_WIDTH_MIN}
            aria-valuemax={NAV_WIDTH_MAX}
            tabIndex={0}
            {...resizeHandleProps}
            sx={{
              position: 'absolute',
              top: 0,
              bottom: 0,
              right: -4,
              width: 8,
              zIndex: 1,
              cursor: 'col-resize',
              touchAction: 'none',
              bgcolor: resizing ? 'primary.main' : 'transparent',
              transition: 'background-color 150ms ease',
              // Grip pill, always visible so the edge reads as draggable.
              '&::before': {
                content: '""',
                position: 'absolute',
                top: '50%',
                left: '50%',
                width: 4,
                height: 40,
                transform: 'translate(-50%, -50%)',
                borderRadius: 2,
                bgcolor: resizing ? 'primary.contrastText' : 'text.disabled',
                transition: 'background-color 150ms ease',
              },
              '&:hover, &:focus-visible': {
                bgcolor: 'primary.main',
                outline: 'none',
                '&::before': { bgcolor: 'primary.contrastText' },
              },
            }}
          />
        )}
      </Box>
    </>
  );
};

export default AppNav;
