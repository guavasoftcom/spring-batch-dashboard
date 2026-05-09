import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import {
  jobCountsMock,
  jobDurationTrendsMock,
  jobLastRunsMock,
  runtimeMock,
  stepCountsMock,
  throughputMock,
} from '~/pages/overview/seedSummary';
import type {
  Durations,
  ExecutionCounts,
  JobDurationSeries,
  JobLastRun,
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

export const getJobDurationTrends = (windowDays: number): Promise<JobDurationSeries[]> =>
  get('/api/overview/job-duration-trends', windowDays, jobDurationTrendsMock);

export const getJobLastRuns = (windowDays: number): Promise<JobLastRun[]> =>
  get('/api/overview/job-last-runs', windowDays, jobLastRunsMock);
