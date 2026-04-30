import { createTheme, type Theme } from '@mui/material/styles';

export type ColorMode = 'light' | 'dark';

export const appColors = {
  brandBlue: '#1565C0',
  brandBlueLight: '#5E92F3',
  brandBlueDark: '#003C8F',
  brandOrange: '#F57C00',
  brandOrangeLight: '#FFAD42',
  brandOrangeDark: '#BB4D00',
  white: '#FFFFFF',
  leafGreen: '#7FD36E',
};

export const pageGradient: Record<ColorMode, string> = {
  light: 'linear-gradient(180deg, #DCE9F7 0%, #6B9BD1 100%)',
  dark: 'linear-gradient(180deg, #0A2D4A 0%, #031626 100%)',
};

export const createAppTheme = (mode: ColorMode): Theme =>
  createTheme({
    palette: {
      mode,
      primary: {
        main: appColors.brandBlue,
        light: appColors.brandBlueLight,
        dark: mode === 'dark' ? '#006bff' : appColors.brandBlueDark,
        contrastText: appColors.white,
      },
      secondary: {
        main: appColors.brandOrange,
        light: appColors.brandOrangeLight,
        dark: appColors.brandOrangeDark,
        contrastText: appColors.white,
      },
      background:
        mode === 'light'
          ? { default: '#F7F9FC', paper: appColors.white }
          : { default: '#0F141A', paper: '#1B2230' },
      divider: mode === 'light' ? '#D5DBE3' : '#2A3440',
    },
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: '"Segoe UI", "Helvetica Neue", Arial, sans-serif',
      h4: { fontWeight: 700 },
      h6: { fontWeight: 700 },
    },
  });

const appTheme = createAppTheme('light');
export default appTheme;
