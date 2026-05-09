import { screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OverviewPage } from '~/pages/overview';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({
  getJobCounts: vi.fn(),
  getStepCounts: vi.fn(),
  getThroughput: vi.fn(),
  getRuntime: vi.fn(),
  getJobDurationTrends: vi.fn(),
  getJobLastRuns: vi.fn(),
}));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

describe('OverviewPage', () => {
  beforeEach(() => {
    apiMocks.getJobCounts.mockResolvedValue({ total: 50, completed: 40, failed: 5, started: 5 });
    apiMocks.getStepCounts.mockResolvedValue({ total: 200, completed: 180, failed: 10, started: 10 });
    apiMocks.getThroughput.mockResolvedValue({ readCount: 1000, writeCount: 950 });
    apiMocks.getRuntime.mockResolvedValue({ averageSeconds: 120, longestSeconds: 600 });
    apiMocks.getJobDurationTrends.mockResolvedValue([
      {
        jobName: 'archiveOrdersJob',
        points: [
          { date: '2026-04-29', averageSeconds: 60 },
          { date: '2026-04-30', averageSeconds: 65 },
        ],
      },
    ]);
    apiMocks.getJobLastRuns.mockResolvedValue([
      {
        jobName: 'importUsersJob',
        run: {
          executionId: 4321,
          status: 'COMPLETED',
          startTime: '2026-04-30T09:15:30Z',
          endTime: '2026-04-30T09:16:30Z',
          durationSeconds: 60,
          readCount: 1000,
          writeCount: 950,
          exitCode: 'COMPLETED',
        },
      },
      { jobName: 'reconcileLedgerJob', run: null },
    ]);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the Overview breadcrumb', () => {
    renderWithProviders(<OverviewPage />, { environment: 'prod' });
    expect(screen.getByText('Overview')).toBeInTheDocument();
  });

  it('shows tile values once the queries resolve', async () => {
    renderWithProviders(<OverviewPage />, { environment: 'prod' });

    await waitFor(() => expect(apiMocks.getJobCounts).toHaveBeenCalled());

    expect(await screen.findByText('Job Executions')).toBeInTheDocument();
    expect(await screen.findByText('Step Executions')).toBeInTheDocument();
    expect(await screen.findByText('Throughput')).toBeInTheDocument();
    expect(await screen.findByText('Runtime')).toBeInTheDocument();
    expect(await screen.findByText('Job Duration Trends')).toBeInTheDocument();
    expect(await screen.findByText('Last Run by Job')).toBeInTheDocument();

    expect(await screen.findByText(/40 completed, 5 failed, 5 active/)).toBeInTheDocument();
    expect(await screen.findByText(/180 completed, 10 failed, 10 active/)).toBeInTheDocument();
    expect(await screen.findByText('2m')).toBeInTheDocument();
    expect(await screen.findByText(/Avg duration .* Longest 10m/)).toBeInTheDocument();
    expect(await screen.findByText('Import Users Job')).toBeInTheDocument();
    expect(await screen.findByText('No runs in this window')).toBeInTheDocument();
  });

  // Smoke check that the JobLastRunsTile renders its Duration column header.
  it('renders the Duration column header in the last-runs table', async () => {
    renderWithProviders(<OverviewPage />, { environment: 'prod' });

    expect((await screen.findAllByText('Duration')).length).toBeGreaterThan(0);
  });

  it('skips the queries when no environment is set', () => {
    renderWithProviders(<OverviewPage />, { environment: '' });

    expect(apiMocks.getJobCounts).not.toHaveBeenCalled();
    expect(apiMocks.getStepCounts).not.toHaveBeenCalled();
  });
});
