import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import {
  jobCountsMock,
  jobLastRunsMock,
  jobStatusChartMock,
  processingMetricsMock,
  runtimeMock,
  stepCountsMock,
  throughputMock,
} from '~/pages/overview/seedSummary';
import type {
  Durations,
  ExecutionCounts,
  JobLastRun,
  JobStatusSlice,
  ThroughputBar,
  ThroughputSummary,
} from '~/types';

const get = async <T>(path: string, windowDays: number, mock: T): Promise<T> => {
  if (USE_MOCK_DATA) {
    return mock;
  }
  const response = await apiClient.get<T>(path, { params: { window: windowDays } });
  return response.data;
};

export const getJobCounts = (windowDays: number): Promise<ExecutionCounts> =>
  get('/api/overview/job-counts', windowDays, jobCountsMock);

export const getStepCounts = (windowDays: number): Promise<ExecutionCounts> =>
  get('/api/overview/step-counts', windowDays, stepCountsMock);

export const getThroughput = (windowDays: number): Promise<ThroughputSummary> =>
  get('/api/overview/throughput', windowDays, throughputMock);

export const getRuntime = (windowDays: number): Promise<Durations> =>
  get('/api/overview/runtime', windowDays, runtimeMock);

export const getJobStatusChart = (windowDays: number): Promise<JobStatusSlice[]> =>
  get('/api/overview/job-status-chart', windowDays, jobStatusChartMock);

export const getProcessingMetrics = (windowDays: number): Promise<ThroughputBar[]> =>
  get('/api/overview/processing-metrics', windowDays, processingMetricsMock);

export const getJobLastRuns = (windowDays: number): Promise<JobLastRun[]> =>
  get('/api/overview/job-last-runs', windowDays, jobLastRunsMock);
