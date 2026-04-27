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

describe('jobRunsApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('real mode', () => {
    it('getRunCounts hits /counts', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { total: 5 } });

      await expect(getRunCounts('jobA')).resolves.toEqual({ total: 5 });
      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/counts');
    });

    it('getSuccessRate hits /success-rate', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { successRate: 80 } });

      await getSuccessRate('jobA');

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/success-rate');
    });

    it('getAvgDuration hits /avg-duration', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { averageSeconds: 1 } });

      await getAvgDuration('jobA');

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/avg-duration');
    });

    it('getLastRun hits /last', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: null });

      await expect(getLastRun('jobA')).resolves.toBeNull();
      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/jobA/runs/last');
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

      await getRunCounts('weird/job name');

      expect(apiClient.get).toHaveBeenCalledWith('/api/jobs/weird%2Fjob%20name/runs/counts');
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

    it('getLastRun returns first sample', async () => {
      await expect(getLastRun('jobA')).resolves.toEqual(sampleRuns[0] ?? null);
      expect(apiClient.get).not.toHaveBeenCalled();
    });

    it('getRunCounts derives from sampleRuns without HTTP', async () => {
      const result = await getRunCounts('jobA');

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.total).toBe(sampleRuns.length);
    });
  });
});
