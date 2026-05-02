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

export const pageGradientBottom: Record<ColorMode, string> = {
  light: '#003C8F',
  dark: '#02080F',
};

export const pageGradient: Record<ColorMode, string> = {
  light: `linear-gradient(180deg, #BFD7EC 0%, #6B9BD1 55%, ${pageGradientBottom.light} 100%)`,
  dark: `linear-gradient(180deg, #0E2238 0%, #06192C 55%, ${pageGradientBottom.dark} 100%)`,
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
