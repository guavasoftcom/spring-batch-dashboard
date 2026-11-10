import { screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import BatchJobsNavContainer from '~/components/BatchJobsNav/BatchJobsNav.container';
import { WindowContext } from '~/shell/WindowContext';
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

  it('requests only the jobs that ran in the selected window', async () => {
    renderWithProviders(
      <WindowContext.Provider value={{ windowDays: 30, setWindowDays: () => {} }}>
        <BatchJobsNavContainer />
      </WindowContext.Provider>,
    );

    await screen.findByText('Import Users Job');
    expect(apiMocks.getJobs).toHaveBeenCalledWith(30);
  });

  it('refetches and drops jobs with no runs when the window changes', async () => {
    apiMocks.getJobs.mockImplementation(async (windowDays: number) =>
      windowDays === 7 ? ['importUsersJob'] : ['importUsersJob', 'reconcileLedgerJob'],
    );
    const tree = (windowDays: number) => (
      <WindowContext.Provider value={{ windowDays, setWindowDays: () => {} }}>
        <BatchJobsNavContainer />
      </WindowContext.Provider>
    );

    const { rerender } = renderWithProviders(tree(30));
    expect(await screen.findByText('Reconcile Ledger Job')).toBeInTheDocument();

    rerender(tree(7));

    await waitFor(() => expect(screen.queryByText('Reconcile Ledger Job')).not.toBeInTheDocument());
    expect(screen.getByText('Import Users Job')).toBeInTheDocument();
    expect(apiMocks.getJobs).toHaveBeenCalledWith(7);
  });
});
