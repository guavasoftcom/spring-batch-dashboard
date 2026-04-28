import { describe, expect, it, vi } from 'vitest';
import { Route, Routes } from 'react-router-dom';
import { screen } from '@testing-library/react';
import AppShellLayout from '~/shell/AppShellLayout';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return {
    ...actual,
    getCurrentUser: vi.fn().mockResolvedValue({ login: 'octo', name: 'Octo Cat', avatarUrl: null }),
    getEnvironments: vi.fn().mockResolvedValue(['prod']),
    getJobs: vi.fn().mockResolvedValue([]),
    logout: vi.fn().mockResolvedValue(undefined),
  };
});

describe('AppShellLayout', () => {
  it('renders the AppShell with the nested route element via Outlet', async () => {
    renderWithProviders(
      <Routes>
        <Route element={<AppShellLayout />}>
          <Route path="/overview" element={<div>nested page body</div>} />
        </Route>
      </Routes>,
      { initialEntries: ['/overview'] },
    );

    expect(await screen.findByText('nested page body')).toBeInTheDocument();
    // Header from AppShell is also visible.
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
  });
});
