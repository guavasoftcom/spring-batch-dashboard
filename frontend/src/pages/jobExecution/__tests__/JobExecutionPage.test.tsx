import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { JobExecutionPage } from '~/pages/jobExecution';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({
  getJobExecutionStepCounts: vi.fn(),
  getExecutionTiming: vi.fn(),
  getStepCountsSummary: vi.fn(),
  getDurationSummary: vi.fn(),
  getStepDetails: vi.fn(),
  getStepExecutionDetail: vi.fn(),
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
  startTime: '2026-04-27T10:00:00Z',
  endTime: '2026-04-27T10:00:30Z',
});

describe('JobExecutionPage', () => {
  beforeEach(() => {
    apiMocks.getJobExecutionStepCounts.mockResolvedValue({ totalSteps: 2, completed: 2, failed: 0, active: 0 });
    apiMocks.getExecutionTiming.mockResolvedValue({
      createTime: '2026-04-27T09:59:58Z',
      startTime: '2026-04-27T10:00:00Z',
      endTime: '2026-04-27T10:00:30Z',
    });
    apiMocks.getStepCountsSummary.mockResolvedValue({
      readCount: 200,
      writeCount: 190,
      commitCount: 2,
      filterCount: 0,
      readSkipCount: 0,
      writeSkipCount: 0,
      processSkipCount: 0,
      rollbackCount: 0,
    });
    apiMocks.getDurationSummary.mockResolvedValue({ totalDurationSeconds: 60 });
    apiMocks.getStepDetails.mockResolvedValue({
      content: [sampleStep(1, 'readUsersStep', 'COMPLETED'), sampleStep(2, 'writeUsersStep', 'COMPLETED')],
      page: 0,
      size: 10,
      totalElements: 2,
    });
    apiMocks.getStepExecutionDetail.mockResolvedValue({
      id: 1,
      jobExecutionId: 7,
      stepName: 'readUsersStep',
      status: 'COMPLETED',
      readCount: 100,
      writeCount: 95,
      commitCount: 1,
      filterCount: 0,
      readSkipCount: 0,
      writeSkipCount: 0,
      processSkipCount: 0,
      rollbackCount: 0,
      durationSeconds: 30,
      createTime: '2026-04-27T09:59:59Z',
      startTime: '2026-04-27T10:00:00Z',
      endTime: '2026-04-27T10:00:30Z',
      lastUpdated: '2026-04-27T10:00:30Z',
      exitCode: 'COMPLETED',
      exitMessage: null,
      executionContext: { checkpoint: 100 },
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

    expect(screen.getByText('Import Users Job')).toBeInTheDocument();
    expect(screen.getByText('Execution #7')).toBeInTheDocument();

    expect((await screen.findAllByText('Steps')).length).toBeGreaterThan(0);
    expect(await screen.findByText('Timing')).toBeInTheDocument();
    expect((await screen.findAllByText('Duration')).length).toBeGreaterThan(0);
    expect((await screen.findAllByText('Status')).length).toBeGreaterThan(0);
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

  it('opens the step detail modal when a step row is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<JobExecutionPage />, {
      environment: 'prod',
      initialEntries: ['/jobs/importUsersJob/executions/7'],
      routePath: '/jobs/:jobId/executions/:executionId',
    });

    await waitFor(() => expect(apiMocks.getStepDetails).toHaveBeenCalled());
    const row = await waitFor(() => {
      const cell = screen.getAllByText('Read Users Step').find((el) => el.closest('tr'));
      const tr = cell?.closest('tr');
      if (!tr) {throw new Error('No step row yet');}
      return tr;
    });
    await user.click(row);

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText(/Read Users Step — Step #1/)).toBeInTheDocument();
    expect(within(dialog).getByText('Execution Context')).toBeInTheDocument();
    expect(within(dialog).getByText('checkpoint')).toBeInTheDocument();
    expect(apiMocks.getStepExecutionDetail).toHaveBeenCalledWith('7', 1);
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
