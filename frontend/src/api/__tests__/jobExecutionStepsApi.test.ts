import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const mockState = { useMock: false };

vi.mock( '~/config/env', () => ({
  get USE_MOCK_DATA() {
    return mockState.useMock;
  },
}));

vi.mock( '~/config/client', () => ({
  apiClient: { get: vi.fn() },
}));

import { apiClient } from '~/config/client';
import { sampleSteps } from '~/pages/jobExecution/seedData';
import {
  getDurationSummary,
  getIoSummary,
  getJobExecutionStepCounts,
  getStepDetails,
} from '~/api/jobExecutionStepsApi';

describe('jobExecutionStepsApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('real mode', () => {
    it('getJobExecutionStepCounts hits /summary/steps', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { totalSteps: 4 } });

      await getJobExecutionStepCounts(42);

      expect(apiClient.get).toHaveBeenCalledWith('/api/job-executions/42/summary/steps');
    });

    it('getIoSummary hits /summary/io', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { totalRead: 0, totalWrite: 0 } });

      await getIoSummary(42);

      expect(apiClient.get).toHaveBeenCalledWith('/api/job-executions/42/summary/io');
    });

    it('getDurationSummary hits /summary/duration', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { totalDurationSeconds: 0 } });

      await getDurationSummary(42);

      expect(apiClient.get).toHaveBeenCalledWith('/api/job-executions/42/summary/duration');
    });

    it('getStepDetails sends sort and pagination params', async () => {
      const page = { content: [], page: 0, size: 10, totalElements: 0 };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: page });

      await getStepDetails(42, 'durationSeconds', 'asc', 2, 5);

      expect(apiClient.get).toHaveBeenCalledWith('/api/job-executions/42/steps', {
        params: { sortBy: 'durationSeconds', sortDir: 'asc', page: 2, size: 5 },
      });
    });

    it('getStepDetails uses defaults when args omitted', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({
        data: { content: [], page: 0, size: 10, totalElements: 0 },
      });

      await getStepDetails(42);

      expect(apiClient.get).toHaveBeenCalledWith('/api/job-executions/42/steps', {
        params: { sortBy: 'startTime', sortDir: 'desc', page: 0, size: 10 },
      });
    });

    it('encodes executionId in path', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { totalSteps: 0 } });

      await getJobExecutionStepCounts('exec/1');

      expect(apiClient.get).toHaveBeenCalledWith('/api/job-executions/exec%2F1/summary/steps');
    });
  });

  describe('mock mode', () => {
    beforeEach(() => {
      mockState.useMock = true;
    });

    it('getJobExecutionStepCounts derives from sampleSteps', async () => {
      const result = await getJobExecutionStepCounts(1);

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.totalSteps).toBe(sampleSteps.length);
      expect(result.completed).toBe(sampleSteps.filter((s) => s.status === 'COMPLETED').length);
      expect(result.failed).toBe(sampleSteps.filter((s) => s.status === 'FAILED').length);
      expect(result.active).toBe(sampleSteps.filter((s) => s.status === 'STARTED').length);
    });

    it('getIoSummary sums sample IO', async () => {
      const result = await getIoSummary(1);

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.totalRead).toBe(sampleSteps.reduce((acc, s) => acc + s.readCount, 0));
      expect(result.totalWrite).toBe(sampleSteps.reduce((acc, s) => acc + s.writeCount, 0));
    });

    it('getDurationSummary sums sample durations', async () => {
      const result = await getDurationSummary(1);

      expect(result.totalDurationSeconds).toBe(
        sampleSteps.reduce((acc, s) => acc + s.durationSeconds, 0),
      );
    });

    it('getStepDetails sorts asc by durationSeconds and paginates locally', async () => {
      const result = await getStepDetails(1, 'durationSeconds', 'asc');

      expect(result.totalElements).toBe(sampleSteps.length);
      const durations = result.content.map((s) => s.durationSeconds);
      expect(durations).toEqual([...durations].sort((a, b) => a - b));
    });

    it('getStepDetails sorts desc by durationSeconds', async () => {
      const result = await getStepDetails(1, 'durationSeconds', 'desc');

      const durations = result.content.map((s) => s.durationSeconds);
      expect(durations).toEqual([...durations].sort((a, b) => b - a));
    });

    it('getStepDetails sends null endTimes to the end on asc sort', async () => {
      const result = await getStepDetails(1, 'endTime', 'asc');

      const lastEnd = result.content[result.content.length - 1].endTime;
      expect(lastEnd).toBeNull();
    });

    it('getStepDetails slices by page/size', async () => {
      const result = await getStepDetails(1, 'startTime', 'desc', 0, 2);

      expect(result.content).toHaveLength(Math.min(2, sampleSteps.length));
      expect(result.size).toBe(2);
      expect(result.page).toBe(0);
      expect(result.totalElements).toBe(sampleSteps.length);
    });
  });
});
