import { createContext, useContext } from 'react';

type WindowContextValue = {
  windowDays: number;
  setWindowDays: (value: number) => void;
};

export const DEFAULT_WINDOW_DAYS = 7;
export const WINDOW_STORAGE_KEY = 'spring-batch-dashboard.window-days';

export const WindowContext = createContext<WindowContextValue>({
  windowDays: DEFAULT_WINDOW_DAYS,
  setWindowDays: () => {},
});

export const useWindow = () => useContext(WindowContext);
