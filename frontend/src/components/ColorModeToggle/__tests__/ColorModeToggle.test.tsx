import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ColorModeProvider } from '~/theme';
import ColorModeToggle from '~/components/ColorModeToggle/ColorModeToggle';

const STORAGE_KEY = 'spring-batch-dashboard.colorMode';

describe('ColorModeToggle', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });
  afterEach(() => {
    window.localStorage.clear();
  });

  it('renders the dark-mode icon when current mode is light', () => {
    render(
      <ColorModeProvider>
        <ColorModeToggle />
      </ColorModeProvider>,
    );
    expect(screen.getByTestId('DarkModeOutlinedIcon')).toBeInTheDocument();
    expect(screen.queryByTestId('LightModeOutlinedIcon')).not.toBeInTheDocument();
  });

  it('toggles the mode and persists to localStorage on click', async () => {
    const user = userEvent.setup();
    render(
      <ColorModeProvider>
        <ColorModeToggle />
      </ColorModeProvider>,
    );

    expect(window.localStorage.getItem(STORAGE_KEY)).toBeNull();

    await user.click(screen.getByRole('button', { name: /toggle color mode/i }));
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe('dark');

    await user.click(screen.getByRole('button', { name: /toggle color mode/i }));
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe('light');
  });

  it('initializes from a persisted dark mode value', () => {
    window.localStorage.setItem(STORAGE_KEY, 'dark');
    render(
      <ColorModeProvider>
        <ColorModeToggle />
      </ColorModeProvider>,
    );
    expect(screen.getByTestId('LightModeOutlinedIcon')).toBeInTheDocument();
    expect(screen.queryByTestId('DarkModeOutlinedIcon')).not.toBeInTheDocument();
  });
});
