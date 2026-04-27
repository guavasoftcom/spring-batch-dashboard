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
import {
  jobCountsMock,
  jobStatusChartMock,
  processingMetricsMock,
  qualitySignalsMock,
  runtimeMock,
  stepCountsMock,
  throughputMock,
} from '~/pages/overview/seedSummary';
import {
  getJobCounts,
  getJobStatusChart,
  getProcessingMetrics,
  getQualitySignals,
  getRuntime,
  getStepCounts,
  getThroughput,
} from '~/api/dashboardApi';

describe('dashboardApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('real mode', () => {
    const cases: Array<[string, () => Promise<unknown>, string, unknown]> = [
      ['getJobCounts', getJobCounts, '/api/overview/job-counts', { total: 1 }],
      ['getStepCounts', getStepCounts, '/api/overview/step-counts', { total: 2 }],
      ['getThroughput', getThroughput, '/api/overview/throughput', { readCount: 3 }],
      ['getRuntime', getRuntime, '/api/overview/runtime', { averageSeconds: 4 }],
      ['getJobStatusChart', getJobStatusChart, '/api/overview/job-status-chart', [{ id: 1 }]],
      ['getProcessingMetrics', getProcessingMetrics, '/api/overview/processing-metrics', [{ metric: 'x' }]],
      ['getQualitySignals', getQualitySignals, '/api/overview/quality-signals', { lastFailure: null }],
    ];

    it.each(cases)('%s hits %s', async (_name, fn, path, payload) => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: payload });

      await expect(fn()).resolves.toEqual(payload);
      expect(apiClient.get).toHaveBeenCalledWith(path);
    });
  });

  describe('mock mode', () => {
    beforeEach(() => {
      mockState.useMock = true;
    });

    it('returns canned mocks without HTTP', async () => {
      await expect(getJobCounts()).resolves.toEqual(jobCountsMock);
      await expect(getStepCounts()).resolves.toEqual(stepCountsMock);
      await expect(getThroughput()).resolves.toEqual(throughputMock);
      await expect(getRuntime()).resolves.toEqual(runtimeMock);
      await expect(getJobStatusChart()).resolves.toEqual(jobStatusChartMock);
      await expect(getProcessingMetrics()).resolves.toEqual(processingMetricsMock);
      await expect(getQualitySignals()).resolves.toEqual(qualitySignalsMock);
      expect(apiClient.get).not.toHaveBeenCalled();
    });
  });
});
