import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import BatchJobsNav from '~/components/BatchJobsNav/BatchJobsNav';

describe('BatchJobsNav', () => {
  it('renders the heading and list of jobs (humanized) when loaded', () => {
    render(
      <BatchJobsNav
        jobs={['importUsersJob', 'reconcileLedgerJob']}
        activeJobId={null}
        loading={false}
        onSelect={() => {}}
      />,
    );

    expect(screen.getByText('Batch Jobs')).toBeInTheDocument();
    expect(screen.getByText(/Import Users/i)).toBeInTheDocument();
    expect(screen.getByText(/Reconcile Ledger/i)).toBeInTheDocument();
  });

  it('shows the empty-state message when there are no jobs and not loading', () => {
    render(<BatchJobsNav jobs={[]} activeJobId={null} loading={false} onSelect={() => {}} />);

    expect(screen.getByText('No jobs found')).toBeInTheDocument();
  });

  it('renders skeleton placeholders while loading and hides job rows', () => {
    render(
      <BatchJobsNav jobs={['importUsersJob']} activeJobId={null} loading onSelect={() => {}} />,
    );

    expect(screen.queryByText(/Import Users/i)).not.toBeInTheDocument();
    expect(screen.queryByText('No jobs found')).not.toBeInTheDocument();
  });

  it('marks the active job as selected', () => {
    render(
      <BatchJobsNav
        jobs={['importUsersJob', 'reconcileLedgerJob']}
        activeJobId="reconcileLedgerJob"
        loading={false}
        onSelect={() => {}}
      />,
    );

    const reconcile = screen.getByRole('button', { name: /Reconcile Ledger/i });
    expect(reconcile).toHaveClass('Mui-selected');
  });

  it('invokes onSelect with the raw jobId when a row is clicked', async () => {
    const user = userEvent.setup();
    const handle = vi.fn();
    render(
      <BatchJobsNav
        jobs={['importUsersJob']}
        activeJobId={null}
        loading={false}
        onSelect={handle}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Import Users/i }));

    expect(handle).toHaveBeenCalledWith('importUsersJob');
  });
});
