import { screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { JobDetailPage } from '~/pages/jobDetail';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({
  getRunCounts: vi.fn(),
  getSuccessRate: vi.fn(),
  getAvgDuration: vi.fn(),
  getLastRun: vi.fn(),
  getRuns: vi.fn(),
  getRunsTrend: vi.fn(),
}));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

const sampleRun = (executionId: number) => ({
  executionId,
  status: 'COMPLETED' as const,
  startTime: '2026-04-27 09:00:00',
  endTime: '2026-04-27 09:01:00',
  durationSeconds: 60,
  readCount: 100,
  writeCount: 95,
  exitCode: 'COMPLETED',
});

describe('JobDetailPage', () => {
  beforeEach(() => {
    apiMocks.getRunCounts.mockResolvedValue({ total: 20, completed: 18, failed: 1, finished: 19 });
    apiMocks.getSuccessRate.mockResolvedValue({ successRate: 95, completed: 18, finished: 19 });
    apiMocks.getAvgDuration.mockResolvedValue({ averageSeconds: 120 });
    apiMocks.getLastRun.mockResolvedValue(sampleRun(7));
    apiMocks.getRuns.mockResolvedValue({
      content: [sampleRun(7), sampleRun(6)],
      page: 0,
      size: 20,
      totalElements: 2,
    });
    apiMocks.getRunsTrend.mockResolvedValue([sampleRun(6), sampleRun(7)]);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders breadcrumb segments and the run-level tiles', async () => {
    renderWithProviders(<JobDetailPage />, {
      environment: 'prod',
      initialEntries: ['/jobs/importUsersJob'],
      routePath: '/jobs/:jobId',
    });

    expect(screen.getByText('Prod')).toBeInTheDocument();
    expect(screen.getByText('Import Users Job')).toBeInTheDocument();

    expect(await screen.findByText('Total Runs')).toBeInTheDocument();
    expect(await screen.findByText('Success Rate')).toBeInTheDocument();
    expect(await screen.findByText('Avg Duration')).toBeInTheDocument();
    expect(await screen.findByText('Last Run')).toBeInTheDocument();
    expect(await screen.findByText('Run Duration Trend')).toBeInTheDocument();
    expect(await screen.findByText('Job Runs')).toBeInTheDocument();

    expect(await screen.findByText(/18 completed, 1 failed/)).toBeInTheDocument();
    expect(await screen.findByText('95%')).toBeInTheDocument();
    expect(await screen.findByText('120s')).toBeInTheDocument();
    expect((await screen.findAllByText('#7')).length).toBeGreaterThan(0);
  });

  it('skips the queries when no environment is set', () => {
    renderWithProviders(<JobDetailPage />, {
      environment: '',
      initialEntries: ['/jobs/importUsersJob'],
      routePath: '/jobs/:jobId',
    });

    expect(apiMocks.getRunCounts).not.toHaveBeenCalled();
  });
});
