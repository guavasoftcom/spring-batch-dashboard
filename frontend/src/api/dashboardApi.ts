import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import {
  jobCountsMock,
  jobStatusChartMock,
  processingMetricsMock,
  qualitySignalsMock,
  runtimeMock,
  stepCountsMock,
  throughputMock,
} from '~/pages/overview/seedSummary';
import type {
  Durations,
  ExecutionCounts,
  JobStatusSlice,
  QualitySignals,
  ThroughputBar,
  ThroughputSummary,
} from '~/types';

const get = async <T>(path: string, mock: T): Promise<T> => {
  if (USE_MOCK_DATA) {
    return mock;
  }
  const response = await apiClient.get<T>(path);
  return response.data;
};

export const getJobCounts = (): Promise<ExecutionCounts> =>
  get('/api/overview/job-counts', jobCountsMock);

export const getStepCounts = (): Promise<ExecutionCounts> =>
  get('/api/overview/step-counts', stepCountsMock);

export const getThroughput = (): Promise<ThroughputSummary> =>
  get('/api/overview/throughput', throughputMock);

export const getRuntime = (): Promise<Durations> =>
  get('/api/overview/runtime', runtimeMock);

export const getJobStatusChart = (): Promise<JobStatusSlice[]> =>
  get('/api/overview/job-status-chart', jobStatusChartMock);

export const getProcessingMetrics = (): Promise<ThroughputBar[]> =>
  get('/api/overview/processing-metrics', processingMetricsMock);

export const getQualitySignals = (): Promise<QualitySignals> =>
  get('/api/overview/quality-signals', qualitySignalsMock);
