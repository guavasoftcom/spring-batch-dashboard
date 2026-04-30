import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { createAppTheme, type ColorMode } from './appTheme';

const COLOR_MODE_STORAGE_KEY = 'spring-batch-dashboard.colorMode';

type ColorModeContextValue = {
  mode: ColorMode;
  toggleMode: () => void;
};

const ColorModeContext = createContext<ColorModeContextValue | undefined>(undefined);

const readStoredMode = (): ColorMode => {
  if (typeof window === 'undefined') {
    return 'light';
  }
  const stored = window.localStorage.getItem(COLOR_MODE_STORAGE_KEY);
  return stored === 'dark' ? 'dark' : 'light';
};

export const ColorModeProvider = ({ children }: { children: ReactNode }) => {
  const [mode, setMode] = useState<ColorMode>(readStoredMode);

  const value = useMemo<ColorModeContextValue>(
    () => ({
      mode,
      toggleMode: () => {
        setMode((current) => {
          const next: ColorMode = current === 'light' ? 'dark' : 'light';
          window.localStorage.setItem(COLOR_MODE_STORAGE_KEY, next);
          return next;
        });
      },
    }),
    [mode],
  );

  const theme = useMemo(() => createAppTheme(mode), [mode]);

  return (
    <ColorModeContext.Provider value={value}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
};

const noopColorMode: ColorModeContextValue = { mode: 'light', toggleMode: () => {} };

export const useColorMode = (): ColorModeContextValue => useContext(ColorModeContext) ?? noopColorMode;
