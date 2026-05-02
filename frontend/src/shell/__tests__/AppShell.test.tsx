import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import AppShell from '~/shell/AppShell';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({
  getCurrentUser: vi.fn(),
  getEnvironments: vi.fn(),
  getJobs: vi.fn(),
  logout: vi.fn(),
}));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

describe('AppShell', () => {
  beforeEach(() => {
    apiMocks.getCurrentUser.mockResolvedValue({
      login: 'octocat',
      name: 'The Octocat',
      avatarUrl: null,
    });
    apiMocks.getEnvironments.mockResolvedValue([
      { name: 'prod', type: 'POSTGRESQL' },
      { name: 'staging', type: 'MYSQL' },
    ]);
    apiMocks.getJobs.mockResolvedValue(['importUsersJob']);
    apiMocks.logout.mockResolvedValue(undefined);
    window.localStorage.clear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the header, user avatar, sidebar nav, and children', async () => {
    renderWithProviders(<AppShell><p>page body</p></AppShell>, { environment: 'prod' });

    expect(screen.getByText('Spring Batch')).toBeInTheDocument();
    expect(await screen.findByText('The Octocat')).toBeInTheDocument();
    expect(screen.getByText('page body')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Overview' })).toBeInTheDocument();
  });

  it('falls back to the login when name is missing', async () => {
    apiMocks.getCurrentUser.mockResolvedValueOnce({ login: 'octocat', name: null, avatarUrl: null });

    renderWithProviders(<AppShell><p>body</p></AppShell>);

    expect(await screen.findByText('octocat')).toBeInTheDocument();
  });

  it('logs out and navigates back to / when Logout is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<AppShell><p>body</p></AppShell>, { initialEntries: ['/overview'] });

    await screen.findByText('The Octocat');
    await user.click(screen.getByRole('button', { name: 'Logout' }));

    await waitFor(() => expect(apiMocks.logout).toHaveBeenCalled());
  });
});
