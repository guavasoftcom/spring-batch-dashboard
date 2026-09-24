import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OverviewPage } from '~/pages/overview';
import { EnvironmentContext } from '~/shell/EnvironmentContext';
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

  describe('charted job selection', () => {
    // Six jobs with distinct averages so the default top-5 is unambiguous:
    // job1Job is the fastest and should be left off the chart.
    const jobNames = ['job1Job', 'job2Job', 'job3Job', 'job4Job', 'job5Job', 'job6Job'];

    beforeEach(() => {
      apiMocks.getJobDurationTrends.mockResolvedValue(
        jobNames.map((jobName, index) => ({
          jobName,
          points: [{ date: '2026-04-30', averageSeconds: (index + 1) * 10 }],
        })),
      );
      apiMocks.getJobLastRuns.mockResolvedValue([
        ...jobNames.map((jobName) => ({ jobName, run: null })),
        { jobName: 'idleJob', run: null },
      ]);
    });

    const checkbox = (label: string) => screen.findByRole('checkbox', { name: `Chart ${label}` });

    it('charts the five longest-running jobs by default', async () => {
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      expect(await screen.findByText('Showing 5 of 6 jobs')).toBeInTheDocument();
      expect(await checkbox('Job1 Job')).not.toBeChecked();
      expect(await checkbox('Job6 Job')).toBeChecked();
    });

    it('disables unchecked jobs once the limit is reached and frees a slot on uncheck', async () => {
      const user = userEvent.setup();
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      expect(await checkbox('Job1 Job')).toBeDisabled();

      await user.click(await checkbox('Job6 Job'));
      expect(await screen.findByText('Showing 4 of 6 jobs')).toBeInTheDocument();
      expect(await checkbox('Job1 Job')).toBeEnabled();

      await user.click(await checkbox('Job1 Job'));
      expect(await screen.findByText('Showing 5 of 6 jobs')).toBeInTheDocument();
      expect(await checkbox('Job1 Job')).toBeChecked();
    });

    it('disables jobs with no duration data in the window', async () => {
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      expect(await checkbox('Idle Job')).toBeDisabled();
    });

    it('prompts for a selection when every job is unchecked', async () => {
      const user = userEvent.setup();
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      for (const label of ['Job2 Job', 'Job3 Job', 'Job4 Job', 'Job5 Job', 'Job6 Job']) {
        await user.click(await checkbox(label));
      }

      expect(await screen.findByText('Select jobs in Last Run by Job to chart them.')).toBeInTheDocument();
      expect(screen.getByText('Showing 0 of 6 jobs')).toBeInTheDocument();
    });

    it('falls back to the default selection after switching environments', async () => {
      const user = userEvent.setup();
      const inEnvironment = (environment: string) => (
        <EnvironmentContext.Provider value={{ environment, setEnvironment: () => {} }}>
          <OverviewPage />
        </EnvironmentContext.Provider>
      );
      const { rerender } = renderWithProviders(inEnvironment('prod'));

      await user.click(await checkbox('Job6 Job'));
      expect(await screen.findByText('Showing 4 of 6 jobs')).toBeInTheDocument();

      rerender(inEnvironment('qa'));
      expect(await screen.findByText('Showing 5 of 6 jobs')).toBeInTheDocument();
      expect(await checkbox('Job6 Job')).toBeChecked();
    });
  });

  it('skips the queries when no environment is set', () => {
    renderWithProviders(<OverviewPage />, { environment: '' });

    expect(apiMocks.getJobCounts).not.toHaveBeenCalled();
    expect(apiMocks.getStepCounts).not.toHaveBeenCalled();
  });
});
