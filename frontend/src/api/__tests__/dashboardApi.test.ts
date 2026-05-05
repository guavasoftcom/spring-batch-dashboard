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

const WINDOW = 7;

describe('dashboardApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('real mode', () => {
    const cases: Array<[string, (windowDays: number) => Promise<unknown>, string, unknown]> = [
      ['getJobCounts', getJobCounts, '/api/overview/job-counts', { total: 1 }],
      ['getStepCounts', getStepCounts, '/api/overview/step-counts', { total: 2 }],
      ['getThroughput', getThroughput, '/api/overview/throughput', { readCount: 3 }],
      ['getRuntime', getRuntime, '/api/overview/runtime', { averageSeconds: 4 }],
      ['getJobStatusChart', getJobStatusChart, '/api/overview/job-status-chart', [{ id: 1 }]],
      ['getProcessingMetrics', getProcessingMetrics, '/api/overview/processing-metrics', [{ metric: 'x' }]],
      ['getQualitySignals', getQualitySignals, '/api/overview/quality-signals', { lastFailure: null }],
    ];

    it.each(cases)('%s hits %s with window param', async (_name, fn, path, payload) => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: payload });

      await expect(fn(WINDOW)).resolves.toEqual(payload);
      expect(apiClient.get).toHaveBeenCalledWith(path, { params: { window: WINDOW } });
    });
  });

  describe('mock mode', () => {
    beforeEach(() => {
      mockState.useMock = true;
    });

    it('returns canned mocks without HTTP', async () => {
      await expect(getJobCounts(WINDOW)).resolves.toEqual(jobCountsMock);
      await expect(getStepCounts(WINDOW)).resolves.toEqual(stepCountsMock);
      await expect(getThroughput(WINDOW)).resolves.toEqual(throughputMock);
      await expect(getRuntime(WINDOW)).resolves.toEqual(runtimeMock);
      await expect(getJobStatusChart(WINDOW)).resolves.toEqual(jobStatusChartMock);
      await expect(getProcessingMetrics(WINDOW)).resolves.toEqual(processingMetricsMock);
      await expect(getQualitySignals(WINDOW)).resolves.toEqual(qualitySignalsMock);
      expect(apiClient.get).not.toHaveBeenCalled();
    });
  });
});
