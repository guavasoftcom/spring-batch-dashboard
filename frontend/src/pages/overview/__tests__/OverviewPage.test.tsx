import { screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OverviewPage } from '~/pages/overview';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({
  getJobCounts: vi.fn(),
  getStepCounts: vi.fn(),
  getThroughput: vi.fn(),
  getRuntime: vi.fn(),
  getJobStatusChart: vi.fn(),
  getProcessingMetrics: vi.fn(),
  getQualitySignals: vi.fn(),
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
    apiMocks.getJobStatusChart.mockResolvedValue([
      { id: 0, label: 'Completed', value: 40, color: '#0a0' },
      { id: 1, label: 'Failed', value: 5, color: '#a00' },
    ]);
    apiMocks.getProcessingMetrics.mockResolvedValue([
      { metric: 'Read', value: 1000 },
      { metric: 'Write', value: 950 },
    ]);
    apiMocks.getQualitySignals.mockResolvedValue({
      lastFailure: 'importUsersJob / readUsersStep',
      latestUpdate: '2026-04-27 10:00:00',
      processing: {
        readCount: 1000, writeCount: 950, commitCount: 100,
        filterCount: 3, rollbackCount: 1, skipCount: 2,
      },
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the breadcrumb with the active environment', () => {
    renderWithProviders(<OverviewPage />, { environment: 'prod' });
    expect(screen.getByText('Prod')).toBeInTheDocument();
    expect(screen.getByText('Overview')).toBeInTheDocument();
  });

  it('shows tile values once the queries resolve', async () => {
    renderWithProviders(<OverviewPage />, { environment: 'prod' });

    await waitFor(() => expect(apiMocks.getJobCounts).toHaveBeenCalled());

    expect(await screen.findByText('Job Executions')).toBeInTheDocument();
    expect(await screen.findByText('Step Executions')).toBeInTheDocument();
    expect(await screen.findByText('Throughput')).toBeInTheDocument();
    expect(await screen.findByText('Runtime')).toBeInTheDocument();
    expect(await screen.findByText('Job Status Distribution')).toBeInTheDocument();
    expect(await screen.findByText('Processing Metrics')).toBeInTheDocument();
    expect(await screen.findByText('Exception & Quality Signals')).toBeInTheDocument();

    expect(await screen.findByText(/40 completed, 5 failed, 5 active/)).toBeInTheDocument();
    expect(await screen.findByText(/180 completed, 10 failed, 10 active/)).toBeInTheDocument();
    expect(await screen.findByText('120s')).toBeInTheDocument();
    expect(await screen.findByText(/Avg duration .* Longest 600s/)).toBeInTheDocument();
    expect(await screen.findByText(/Last failure: Import Users Job/)).toBeInTheDocument();
    expect(await screen.findByText(/Rollbacks: 1.*Skips: 2.*Filters: 3/)).toBeInTheDocument();
  });

  it('skips the queries when no environment is set', () => {
    renderWithProviders(<OverviewPage />, { environment: '' });

    expect(apiMocks.getJobCounts).not.toHaveBeenCalled();
    expect(apiMocks.getStepCounts).not.toHaveBeenCalled();
  });
});
