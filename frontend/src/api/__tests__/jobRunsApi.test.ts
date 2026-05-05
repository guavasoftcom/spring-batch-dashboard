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
import { sampleRuns } from '~/pages/jobDetail/components/seedData';
import {
  getAvgDuration,
  getLastRun,
  getRunCounts,
  getRuns,
  getRunsTrend,
  getSuccessRate,
} from '~/api/jobRunsApi';

const ALL_RUNS_WINDOW = 30;

describe('jobRunsApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('real mode', () => {
    it('getRunCounts hits /counts with window param', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { total: 5 } });

      await expect(getRunCounts('jobA', 7)).resolves.toEqual({ total: 5 });
      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/counts', {
        params: { window: 7 },
      });
    });

    it('getSuccessRate hits /success-rate with window param', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { successRate: 80 } });

      await getSuccessRate('jobA', 30);

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/success-rate', {
        params: { window: 30 },
      });
    });

    it('getAvgDuration hits /avg-duration with window param', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { averageSeconds: 1 } });

      await getAvgDuration('jobA', 60);

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/avg-duration', {
        params: { window: 60 },
      });
    });

    it('getLastRun hits /last with window param', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: null });

      await expect(getLastRun('jobA', 7)).resolves.toBeNull();
      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/last', {
        params: { window: 7 },
      });
    });

    it('getRuns sends sort and pagination params', async () => {
      const page = { content: [], page: 0, size: 20, totalElements: 0 };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: page });

      await getRuns('jobA', 'startTime', 'asc', 2, 10);

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs', {
        params: { sortBy: 'startTime', sortDir: 'asc', page: 2, size: 10 },
      });
    });

    it('getRuns uses defaults when args omitted', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({
        data: { content: [], page: 0, size: 20, totalElements: 0 },
      });

      await getRuns('jobA');

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs', {
        params: { sortBy: 'executionId', sortDir: 'desc', page: 0, size: 20 },
      });
    });

    it('getRunsTrend sends window param', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] });

      await getRunsTrend('jobA', 7);

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/trend', {
        params: { window: 7 },
      });
    });

    it('encodes jobId in path', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { total: 0 } });

      await getRunCounts('weird/job name', 7);

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/weird%2Fjob%20name/runs/counts', {
        params: { window: 7 },
      });
    });
  });

  describe('mock mode', () => {
    beforeEach(() => {
      mockState.useMock = true;
    });

    it('getRuns sorts and paginates locally', async () => {
      const result = await getRuns('jobA', 'executionId', 'desc', 0, 2);

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.totalElements).toBe(sampleRuns.length);
      expect(result.content).toHaveLength(Math.min(2, sampleRuns.length));
      const ids = result.content.map((r) => r.executionId);
      expect(ids).toEqual([...ids].sort((a, b) => b - a));
    });

    it('getLastRun returns first sample within window', async () => {
      const result = await getLastRun('jobA', ALL_RUNS_WINDOW);
      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result).not.toBeNull();
    });

    it('getRunCounts derives from sampleRuns without HTTP', async () => {
      const result = await getRunCounts('jobA', ALL_RUNS_WINDOW);

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.total).toBe(sampleRuns.length);
    });

    it('getRunsTrend filters samples without HTTP', async () => {
      const result = await getRunsTrend('jobA', 30);

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(Array.isArray(result)).toBe(true);
    });

    it('getRuns sorts by a field with null values (asc, null-last)', async () => {
      const result = await getRuns('jobA', 'endTime', 'asc', 0, sampleRuns.length);

      // Null endTimes should sort to the end regardless of direction.
      expect(result.content.at(-1)?.endTime).toBeNull();
    });

    it('getRuns ascending sort orders smallest first', async () => {
      const result = await getRuns('jobA', 'executionId', 'asc', 0, sampleRuns.length);

      const ids = result.content.map((r) => r.executionId);
      expect(ids).toEqual([...ids].sort((a, b) => a - b));
    });

    it('getRuns honours equal values without crashing', async () => {
      // readCount has duplicates (e.g. two rows with 5012); the tie path returns 0.
      const result = await getRuns('jobA', 'readCount', 'desc', 0, sampleRuns.length);

      expect(result.content).toHaveLength(sampleRuns.length);
    });
  });
});
