import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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
    expect(await screen.findByText('Jobs')).toBeInTheDocument();

    expect(await screen.findByText(/40 completed, 5 failed, 5 active/)).toBeInTheDocument();
    expect(await screen.findByText(/180 completed, 10 failed, 10 active/)).toBeInTheDocument();
    expect(await screen.findByText('2m')).toBeInTheDocument();
    expect(await screen.findByText(/Avg duration .* Longest 10m/)).toBeInTheDocument();
    expect(await screen.findByText('Import Users Job')).toBeInTheDocument();
    expect(await screen.findByText('No runs in this window')).toBeInTheDocument();
  });

  it('renders the Duration column header in the jobs table', async () => {
    renderWithProviders(<OverviewPage />, { environment: 'prod' });

    expect((await screen.findAllByText('Duration')).length).toBeGreaterThan(0);
  });

  describe('job table', () => {
    const run = (executionId: number, status: string, durationSeconds: number, startTime: string) => ({
      executionId,
      status,
      startTime,
      endTime: status === 'STARTED' ? null : startTime,
      durationSeconds,
      readCount: 10,
      writeCount: 9,
      exitCode: status,
    });

    beforeEach(() => {
      apiMocks.getJobDurationTrends.mockResolvedValue([
        'slowJob',
        'steadyJob',
        'brokenJob',
        'busyJob',
      ].map((jobName) => ({
        jobName,
        points: [
          { date: '2026-04-28', averageSeconds: 100 },
          { date: '2026-04-29', averageSeconds: 100 },
          { date: '2026-04-30', averageSeconds: 100 },
        ],
      })));
      apiMocks.getJobLastRuns.mockResolvedValue([
        { jobName: 'steadyJob', run: run(1, 'COMPLETED', 100, '2026-04-30T09:00:00Z') },
        { jobName: 'slowJob', run: run(2, 'COMPLETED', 200, '2026-04-30T08:00:00Z') },
        { jobName: 'brokenJob', run: run(3, 'FAILED', 20, '2026-04-30T07:00:00Z') },
        { jobName: 'busyJob', run: run(4, 'STARTED', 30, '2026-04-30T06:00:00Z') },
        { jobName: 'idleJob', run: null },
      ]);
    });

    const jobOrder = async () => {
      await screen.findByText('Steady Job');
      return screen
        .getAllByRole('button', { name: /^(Steady|Slow|Broken|Busy|Idle) Job$/ })
        .map((button) => button.textContent);
    };

    it('lists problems first: failed, running, slower than usual, then the rest', async () => {
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      expect(await jobOrder()).toEqual(['Broken Job', 'Busy Job', 'Slow Job', 'Steady Job', 'Idle Job']);
    });

    it('shows how far the last run is from usual', async () => {
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      expect(await screen.findByText('▲ 100%')).toBeInTheDocument();
      expect(screen.getByText('0%')).toBeInTheDocument();
    });

    it('filters by status chip and shows counts', async () => {
      const user = userEvent.setup();
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      await user.click(await screen.findByRole('button', { name: 'Failed 1' }));
      expect(screen.getByText('Broken Job')).toBeInTheDocument();
      expect(screen.queryByText('Steady Job')).not.toBeInTheDocument();

      await user.click(screen.getByRole('button', { name: 'Slower than usual 1' }));
      expect(screen.getByText('Slow Job')).toBeInTheDocument();
      expect(screen.queryByText('Broken Job')).not.toBeInTheDocument();
    });

    it('filters by search text and explains when nothing matches', async () => {
      const user = userEvent.setup();
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      await user.type(await screen.findByRole('searchbox', { name: 'Search jobs' }), 'zzz');
      expect(await screen.findByText('No jobs match these filters.')).toBeInTheDocument();
    });

    it('sorts by name', async () => {
      const user = userEvent.setup();
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      await screen.findByText('Steady Job');
      await user.click(screen.getByRole('combobox', { name: 'Sort jobs' }));
      await user.click(await screen.findByRole('option', { name: 'Name' }));

      expect(await jobOrder()).toEqual(['Broken Job', 'Busy Job', 'Idle Job', 'Slow Job', 'Steady Job']);
    });

    it('expands a row to show its duration chart, and collapses it again', async () => {
      const user = userEvent.setup();
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      const toggle = await screen.findByRole('button', { name: 'Show duration chart for Steady Job' });
      expect(toggle).toHaveAttribute('aria-expanded', 'false');

      await user.click(toggle);
      expect(toggle).toHaveAttribute('aria-expanded', 'true');

      await user.click(toggle);
      expect(toggle).toHaveAttribute('aria-expanded', 'false');
    });

    it('does not offer a chart for a job with no duration data', async () => {
      renderWithProviders(<OverviewPage />, { environment: 'prod' });

      expect(await screen.findByRole('button', { name: 'Show duration chart for Idle Job' })).toBeDisabled();
    });
  });

  it('skips the queries when no environment is set', () => {
    renderWithProviders(<OverviewPage />, { environment: '' });

    expect(apiMocks.getJobCounts).not.toHaveBeenCalled();
    expect(apiMocks.getStepCounts).not.toHaveBeenCalled();
  });
});
