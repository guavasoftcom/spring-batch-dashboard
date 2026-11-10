import { useMediaQuery, useTheme } from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useNav } from '../NavContext';
import AppNav from './AppNav';
import { useNavResize } from './useNavResize';

const AppNavContainer = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isWide = useMediaQuery(theme.breakpoints.up('md'));
  const { navOpen, setNavOpen } = useNav();
  const collapsed = isWide && !navOpen;
  const onDashboard = location.pathname === '/overview';
  const { width, resizing, handleProps } = useNavResize();

  return (
    <AppNav
      navOpen={navOpen}
      isWide={isWide}
      collapsed={collapsed}
      width={width}
      resizing={resizing}
      resizeHandleProps={handleProps}
      onDashboard={onDashboard}
      onBackdropClick={() => setNavOpen(false)}
      onNavigateOverview={() => navigate('/overview')}
    />
  );
};

export default AppNavContainer;
