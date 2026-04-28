import { screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import BatchJobsNavContainer from '~/components/BatchJobsNav/BatchJobsNav.container';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({ getJobs: vi.fn() }));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

describe('BatchJobsNav container', () => {
  beforeEach(() => {
    apiMocks.getJobs.mockResolvedValue(['importUsersJob', 'reconcileLedgerJob']);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the jobs returned by the api', async () => {
    renderWithProviders(<BatchJobsNavContainer />);

    expect(await screen.findByText('Import Users Job')).toBeInTheDocument();
    expect(await screen.findByText('Reconcile Ledger Job')).toBeInTheDocument();
  });

  it('passes the active jobId from the route into the presentational component', async () => {
    renderWithProviders(<BatchJobsNavContainer />, {
      initialEntries: ['/jobs/reconcileLedgerJob'],
      routePath: '/jobs/:jobId',
    });

    const reconcile = await screen.findByRole('button', { name: /Reconcile Ledger Job/i });
    expect(reconcile).toHaveClass('Mui-selected');
  });
});
