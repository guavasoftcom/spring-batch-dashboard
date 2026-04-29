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
});

describe('pageGradient', () => {
  it('has a distinct gradient for each mode', () => {
    expect(pageGradient.light).toBeTruthy();
    expect(pageGradient.dark).toBeTruthy();
    expect(pageGradient.light).not.toBe(pageGradient.dark);
  });
});
