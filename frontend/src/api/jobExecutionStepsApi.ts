import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import {
  computeDurationSummary,
  computeIoSummary,
  computeStepCounts,
  computeStepDurations,
  sampleSteps,
} from '~/pages/jobExecution/seedData';
import type {
  DurationSummary,
  IoSummary,
  JobExecutionStepCounts,
  StepDetailPage,
  StepDuration,
} from '~/pages/jobExecution/types';

export type StepSortField =
  | 'stepName'
  | 'status'
  | 'readCount'
  | 'writeCount'
  | 'skipCount'
  | 'rollbackCount'
  | 'durationSeconds'
  | 'startTime'
  | 'endTime';

export type StepSortDir = 'asc' | 'desc';

const base = (executionId: string | number) =>
  `/api/job-executions/${encodeURIComponent(String(executionId))}`;

export const getJobExecutionStepCounts = async (
  executionId: string | number,
): Promise<JobExecutionStepCounts> => {
  if (USE_MOCK_DATA) {
    return computeStepCounts(sampleSteps);
  }
  const response = await apiClient.get<JobExecutionStepCounts>(`${base(executionId)}/summary/steps`);
  return response.data;
};

export const getIoSummary = async (executionId: string | number): Promise<IoSummary> => {
  if (USE_MOCK_DATA) {
    return computeIoSummary(sampleSteps);
  }
  const response = await apiClient.get<IoSummary>(`${base(executionId)}/summary/io`);
  return response.data;
};

export const getDurationSummary = async (executionId: string | number): Promise<DurationSummary> => {
  if (USE_MOCK_DATA) {
    return computeDurationSummary(sampleSteps);
  }
  const response = await apiClient.get<DurationSummary>(`${base(executionId)}/summary/duration`);
  return response.data;
};

export const getStepDurations = async (executionId: string | number): Promise<StepDuration[]> => {
  if (USE_MOCK_DATA) {
    return computeStepDurations(sampleSteps);
  }
  const response = await apiClient.get<StepDuration[]>(`${base(executionId)}/step-durations`);
  return response.data;
};

export const getStepDetails = async (
  executionId: string | number,
  sortBy: StepSortField = 'startTime',
  sortDir: StepSortDir = 'desc',
  page = 0,
  size = 10,
): Promise<StepDetailPage> => {
  if (USE_MOCK_DATA) {
    const sorted = [...sampleSteps].sort((a, b) => {
      const av = a[sortBy];
      const bv = b[sortBy];
      if (av === bv) { return 0; }
      if (av === null || av === undefined) { return 1; }
      if (bv === null || bv === undefined) { return -1; }
      const cmp = av < bv ? -1 : 1;
      return sortDir === 'asc' ? cmp : -cmp;
    });
    const start = page * size;
    return {
      content: sorted.slice(start, start + size),
      page,
      size,
      totalElements: sorted.length,
    };
  }
  const response = await apiClient.get<StepDetailPage>(`${base(executionId)}/steps`, {
    params: { sortBy, sortDir, page, size },
  });
  return response.data;
};
