import { createContext, useContext } from 'react';
import type { ColorMode } from './appTheme';

export type ColorModeContextValue = {
  mode: ColorMode;
  toggleMode: () => void;
};

export const ColorModeContext = createContext<ColorModeContextValue | undefined>(undefined);

const noopColorMode: ColorModeContextValue = { mode: 'light', toggleMode: () => {} };

export const useColorMode = (): ColorModeContextValue => useContext(ColorModeContext) ?? noopColorMode;
