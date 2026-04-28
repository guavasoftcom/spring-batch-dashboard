import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { JobExecutionPage } from '~/pages/jobExecution';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({
  getJobExecutionStepCounts: vi.fn(),
  getIoSummary: vi.fn(),
  getDurationSummary: vi.fn(),
  getStepDurations: vi.fn(),
  getStepDetails: vi.fn(),
}));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

const sampleStep = (id: number, stepName: string, status: 'COMPLETED' | 'FAILED' | 'STARTED') => ({
  id,
  stepName,
  status,
  readCount: 100,
  writeCount: 95,
  skipCount: 0,
  rollbackCount: 0,
  durationSeconds: 30,
  startTime: '2026-04-27 10:00:00',
  endTime: '2026-04-27 10:00:30',
  exitCode: status,
  exitMessage: '',
  context: {},
});

describe('JobExecutionPage', () => {
  beforeEach(() => {
    apiMocks.getJobExecutionStepCounts.mockResolvedValue({ totalSteps: 2, completed: 2, failed: 0, active: 0 });
    apiMocks.getIoSummary.mockResolvedValue({ totalRead: 200, totalWrite: 190 });
    apiMocks.getDurationSummary.mockResolvedValue({ totalDurationSeconds: 60 });
    apiMocks.getStepDurations.mockResolvedValue([
      { stepName: 'readUsersStep', durationSeconds: 30 },
      { stepName: 'writeUsersStep', durationSeconds: 30 },
    ]);
    apiMocks.getStepDetails.mockResolvedValue({
      content: [sampleStep(1, 'readUsersStep', 'COMPLETED'), sampleStep(2, 'writeUsersStep', 'COMPLETED')],
      page: 0,
      size: 10,
      totalElements: 2,
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders breadcrumb segments and execution-level tiles', async () => {
    renderWithProviders(<JobExecutionPage />, {
      environment: 'prod',
      initialEntries: ['/jobs/importUsersJob/executions/7'],
      routePath: '/jobs/:jobId/executions/:executionId',
    });

    expect(screen.getByText('Prod')).toBeInTheDocument();
    expect(screen.getByText('Import Users Job')).toBeInTheDocument();
    expect(screen.getByText('Execution #7')).toBeInTheDocument();

    expect((await screen.findAllByText('Steps')).length).toBeGreaterThan(0);
    expect(await screen.findByText('Read / Write')).toBeInTheDocument();
    expect((await screen.findAllByText('Duration')).length).toBeGreaterThan(0);
    expect((await screen.findAllByText('Status')).length).toBeGreaterThan(0);
    expect(await screen.findByText('Step Durations')).toBeInTheDocument();
  });

  it('navigates back to the parent job page when the back link is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<JobExecutionPage />, {
      environment: 'prod',
      initialEntries: ['/jobs/importUsersJob/executions/7'],
      routePath: '/jobs/:jobId/executions/:executionId',
    });

    const backButton = await screen.findByRole('button', { name: /Back to Import Users Job/i });
    await user.click(backButton);
  });

  it('skips queries when no environment is set', () => {
    renderWithProviders(<JobExecutionPage />, {
      environment: '',
      initialEntries: ['/jobs/importUsersJob/executions/7'],
      routePath: '/jobs/:jobId/executions/:executionId',
    });

    expect(apiMocks.getJobExecutionStepCounts).not.toHaveBeenCalled();
  });
});
