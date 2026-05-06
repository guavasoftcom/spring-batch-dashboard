import { describe, expect, it } from 'vitest';
import { createAppTheme, pageGradient } from '~/theme';

describe('createAppTheme', () => {
  it('uses #003C8F as primary.dark in light mode', () => {
    const theme = createAppTheme('light');
    expect(theme.palette.mode).toBe('light');
    expect(theme.palette.primary.dark.toUpperCase()).toBe('#003C8F');
  });

  it('uses #006bff as primary.dark in dark mode', () => {
    const theme = createAppTheme('dark');
    expect(theme.palette.mode).toBe('dark');
    expect(theme.palette.primary.dark.toLowerCase()).toBe('#006bff');
  });

  it('exposes a different background.paper per mode', () => {
    const light = createAppTheme('light').palette.background.paper;
    const dark = createAppTheme('dark').palette.background.paper;
    expect(light).not.toBe(dark);
  });

  // Status chips (`<Chip color="success|error|info">`) rely on the semantic palette's
  // contrastText for their label color. MUI's auto-derivation produced dark text against
  // the light-mode mains, which made the badges unreadable; pin to white in both modes.
  it.each(['light', 'dark'] as const)('locks success/error/info contrastText to white in %s mode', (mode) => {
    const theme = createAppTheme(mode);
    expect(theme.palette.success.contrastText).toBe('#FFFFFF');
    expect(theme.palette.error.contrastText).toBe('#FFFFFF');
    expect(theme.palette.info.contrastText).toBe('#FFFFFF');
  });
});

describe('pageGradient', () => {
  it('has a distinct gradient for each mode', () => {
    expect(pageGradient.light).toBeTruthy();
    expect(pageGradient.dark).toBeTruthy();
    expect(pageGradient.light).not.toBe(pageGradient.dark);
  });
});
