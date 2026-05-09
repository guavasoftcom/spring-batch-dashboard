import { createTheme, type Theme } from '@mui/material/styles';
import '@mui/x-charts/themeAugmentation';

declare module '@mui/material/styles' {
  interface Palette {
    surface: { inset: string };
  }
  interface PaletteOptions {
    surface?: { inset?: string };
  }
}

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

/**
 * Categorical palette used for "one color per item" UIs — multi-series chart lines, the
 * job icons in the side nav, etc. Colors are chosen to stay clear of the blue / green
 * (#66BB6A) / orange (#FFA726) hues that the run-duration trend chart already uses for
 * its Duration / Read / Write series, so two side-by-side trend tiles don't compete.
 *
 * Order alternates warm ↔ cool and bright ↔ muted so adjacent indexes give strong
 * line-to-line contrast in a stacked chart, while the sequence as a whole still flows
 * visually (no two same-family colors land next to each other). Consumers cycle by
 * index: `categoricalPalette[i % categoricalPalette.length]`.
 */
export const categoricalPalette: readonly string[] = [
  '#EF5350', // red          — warm, bright
  '#5E35B1', // deep purple  — cool, dark
  '#FFCA28', // amber/gold   — warm, light
  '#78909C', // blue grey    — cool, muted
  '#D81B60', // magenta      — warm, vivid
  '#9E9D24', // olive        — yellow-green, muted
  '#AB47BC', // purple       — cool, bright
  '#8D6E63', // brown        — neutral warm, muted
  '#EC407A', // pink         — warm, bright
  '#5D4037', // dark brown   — neutral, dark
];

export const pageGradientBottom: Record<ColorMode, string> = {
  light: '#003C8F',
  dark: '#02080F',
};

export const pageGradient: Record<ColorMode, string> = {
  light: `linear-gradient(180deg, #BFD7EC 0%, #6B9BD1 55%, ${pageGradientBottom.light} 100%)`,
  dark: `linear-gradient(180deg, #0E2238 0%, #06192C 55%, ${pageGradientBottom.dark} 100%)`,
};

const chartTextFill = (theme: Theme) => (theme.palette.mode === 'dark' ? '#FFFFFF' : '#37474F');

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
      success: { main: '#2E7D32', contrastText: appColors.white },
      error: { main: '#D32F2F', contrastText: appColors.white },
      info: { main: '#0288D1', contrastText: appColors.white },
      background:
        mode === 'light'
          ? { default: '#F7F9FC', paper: appColors.white }
          : { default: '#0F141A', paper: '#1B2230' },
      divider: mode === 'light' ? '#D5DBE3' : '#2A3440',
      // Subtly inset card background — used by the StepDetailModal StatGrid to render
      // data cards on top of the dialog Paper.
      surface: { inset: mode === 'light' ? '#F7F9FC' : '#2A3440' },
    },
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: '"Segoe UI", "Helvetica Neue", Arial, sans-serif',
      h4: { fontWeight: 700 },
      h6: { fontWeight: 700 },
    },
    components: {
      MuiChartsAxis: {
        styleOverrides: {
          root: ({ theme }) => ({
            '& .MuiChartsAxis-tickLabel': { fill: chartTextFill(theme) },
            '& .MuiChartsAxis-label': { fill: chartTextFill(theme) },
          }),
        },
      },
      MuiChartsLegend: {
        styleOverrides: {
          root: ({ theme }) => ({
            '& .MuiChartsLegend-label': { fill: chartTextFill(theme) },
          }),
        },
      },
      MuiChartsTooltip: {
        styleOverrides: {
          paper: ({ theme }) => ({
            backgroundColor: theme.palette.background.paper,
            border: `1px solid ${theme.palette.divider}`,
          }),
          // Scoped to the default tooltip's own cells so we don't recolor descendants
          // of custom tooltips that happen to live inside ChartsTooltipContainer
          // (e.g. status Chips in RunStatusTooltip).
          cell: ({ theme }) => ({ color: theme.palette.text.primary }),
          axisValueCell: ({ theme }) => ({ color: theme.palette.text.primary, fontWeight: 700 }),
        },
      },
    },
  });

const appTheme = createAppTheme('light');
export default appTheme;
