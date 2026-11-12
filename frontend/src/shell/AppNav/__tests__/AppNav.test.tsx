import { fireEvent, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { NavContext } from '~/shell/NavContext';
import { renderWithProviders } from '~/test-utils/renderWithProviders';
import { AppNav } from '..';
import { NAV_WIDTH_STORAGE_KEY } from '../useNavResize';

const apiMocks = vi.hoisted(() => ({
  getEnvironments: vi.fn(),
  getJobs: vi.fn(),
}));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

const stubMatchMedia = (matches: boolean) => {
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches,
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    addListener: vi.fn(),
    removeListener: vi.fn(),
  }));
};

const renderNav = (navOpen = true) =>
  renderWithProviders(
    <NavContext.Provider value={{ navOpen, setNavOpen: () => {} }}>
      <AppNav />
    </NavContext.Provider>,
  );

describe('AppNav resize handle', () => {
  beforeEach(() => {
    apiMocks.getEnvironments.mockResolvedValue([{ name: 'prod', type: 'POSTGRESQL' }]);
    apiMocks.getJobs.mockResolvedValue(['importUsersJob']);
    window.localStorage.clear();
    stubMatchMedia(true);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders an accessible separator at the default 240px width', () => {
    renderNav();

    const handle = screen.getByRole('separator', { name: 'Resize navigation' });
    expect(handle).toHaveAttribute('aria-orientation', 'vertical');
    expect(handle).toHaveAttribute('aria-valuenow', '240');
    expect(handle).toHaveAttribute('aria-valuemin', '200');
    expect(handle).toHaveAttribute('aria-valuemax', '480');
    expect(screen.getByRole('navigation')).toHaveStyle({ width: '240px' });
  });

  it('applies a stored width on mount', () => {
    window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, '360');

    renderNav();

    expect(screen.getByRole('navigation')).toHaveStyle({ width: '360px' });
    expect(screen.getByRole('separator')).toHaveAttribute('aria-valuenow', '360');
  });

  it('resizes with the keyboard and resets on double click', async () => {
    const user = userEvent.setup();
    renderNav();
    const handle = screen.getByRole('separator');

    handle.focus();
    await user.keyboard('{ArrowRight}');
    expect(handle).toHaveAttribute('aria-valuenow', '256');
    expect(screen.getByRole('navigation')).toHaveStyle({ width: '256px' });

    await user.dblClick(handle);
    expect(handle).toHaveAttribute('aria-valuenow', '240');
  });

  it('resizes by dragging and persists the width on release', () => {
    renderNav();
    const handle = screen.getByRole('separator');

    fireEvent.pointerDown(handle, { clientX: 240, pointerId: 1 });
    fireEvent.pointerMove(handle, { clientX: 320, pointerId: 1 });
    expect(screen.getByRole('navigation')).toHaveStyle({ width: '320px' });
    expect(screen.getByRole('navigation')).toHaveStyle({ transition: 'none' });

    fireEvent.pointerUp(handle, { clientX: 320, pointerId: 1 });
    expect(window.localStorage.getItem(NAV_WIDTH_STORAGE_KEY)).toBe('320');
  });

  it('hides the handle when the nav is collapsed', () => {
    renderNav(false);

    expect(screen.queryByRole('separator')).not.toBeInTheDocument();
    expect(screen.getByRole('navigation')).toHaveStyle({ width: '64px' });
  });

  it('hides the handle and keeps the drawer at 240px below the md breakpoint', () => {
    stubMatchMedia(false);
    window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, '400');

    renderNav();

    expect(screen.queryByRole('separator')).not.toBeInTheDocument();
    expect(screen.getByRole('navigation')).toHaveStyle({ width: '240px' });
  });
});
