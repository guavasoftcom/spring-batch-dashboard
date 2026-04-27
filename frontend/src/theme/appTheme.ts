import { createTheme } from '@mui/material/styles';

export const appColors = {
  brandBlue: '#1565C0',
  brandBlueLight: '#5E92F3',
  brandBlueDark: '#003C8F',
  brandOrange: '#F57C00',
  brandOrangeLight: '#FFAD42',
  brandOrangeDark: '#BB4D00',
  pageBackground: '#F4F8FF',
  white: '#FFFFFF',
  loginGradient:
    'radial-gradient(circle at 12% 18%, rgba(245, 124, 0, 0.24), transparent 40%), radial-gradient(circle at 88% 80%, rgba(21, 101, 192, 0.3), transparent 45%), linear-gradient(140deg, #031626 0%, #0A2D4A 50%, #123A5C 100%)',
  glassBorder: 'rgba(255,255,255,0.18)',
  glassBackground: 'rgba(255,255,255,0.08)',
  textOnDark: '#EAF4FF',
  leafGreen: '#7FD36E',
};

const appTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: appColors.brandBlue,
      light: appColors.brandBlueLight,
      dark: appColors.brandBlueDark,
      contrastText: appColors.white,
    },
    secondary: {
      main: appColors.brandOrange,
      light: appColors.brandOrangeLight,
      dark: appColors.brandOrangeDark,
      contrastText: appColors.white,
    },
    background: {
      default: appColors.pageBackground,
      paper: appColors.white,
    },
  },
  shape: {
    borderRadius: 12,
  },
  typography: {
    fontFamily: '"Segoe UI", "Helvetica Neue", Arial, sans-serif',
    h4: {
      fontWeight: 700,
    },
    h6: {
      fontWeight: 700,
    },
  },
});

export default appTheme;
