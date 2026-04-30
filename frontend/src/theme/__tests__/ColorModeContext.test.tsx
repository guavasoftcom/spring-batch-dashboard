import { act, render, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { useTheme } from '@mui/material';
import { ColorModeProvider, useColorMode } from '~/theme';

const STORAGE_KEY = 'spring-batch-dashboard.colorMode';

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <ColorModeProvider>{children}</ColorModeProvider>
);

describe('ColorModeContext', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });
  afterEach(() => {
    window.localStorage.clear();
  });

  it('defaults to light mode when no value is stored', () => {
    const { result } = renderHook(() => useColorMode(), { wrapper });
    expect(result.current.mode).toBe('light');
  });

  it('reads the persisted mode on init', () => {
    window.localStorage.setItem(STORAGE_KEY, 'dark');
    const { result } = renderHook(() => useColorMode(), { wrapper });
    expect(result.current.mode).toBe('dark');
  });

  it('toggles between light and dark and persists each change', () => {
    const { result } = renderHook(() => useColorMode(), { wrapper });

    act(() => result.current.toggleMode());
    expect(result.current.mode).toBe('dark');
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe('dark');

    act(() => result.current.toggleMode());
    expect(result.current.mode).toBe('light');
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe('light');
  });

  it('returns a no-op default when used outside the provider', () => {
    const { result } = renderHook(() => useColorMode());
    expect(result.current.mode).toBe('light');
    expect(() => result.current.toggleMode()).not.toThrow();
  });

  it('applies a dark MUI theme when mode is dark', () => {
    window.localStorage.setItem(STORAGE_KEY, 'dark');
    let observed: 'light' | 'dark' | undefined;
    const Probe = () => {
      observed = useTheme().palette.mode;
      return null;
    };
    render(
      <ColorModeProvider>
        <Probe />
      </ColorModeProvider>,
    );
    expect(observed).toBe('dark');
  });
});
