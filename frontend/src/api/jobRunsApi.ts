import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import {
  computeAvgDuration,
  computeRunCounts,
  computeSuccessRate,
  filterTrendRuns,
  sampleRuns,
} from '~/pages/jobDetail/components/seedData';
import type { AvgDuration, JobRun, RunCounts, SuccessRate } from '~/types';

const base = (jobId: string) => `/api/jobs/${encodeURIComponent(jobId)}/runs`;

export const getRunCounts = async (jobId: string): Promise<RunCounts> => {
  if (USE_MOCK_DATA) {
    return computeRunCounts(sampleRuns);
  }
  const response = await apiClient.get<RunCounts>(`${base(jobId)}/counts`);
  return response.data;
};

export const getSuccessRate = async (jobId: string): Promise<SuccessRate> => {
  if (USE_MOCK_DATA) {
    return computeSuccessRate(sampleRuns);
  }
  const response = await apiClient.get<SuccessRate>(
    `${base(jobId)}/success-rate`,
  );
  return response.data;
};

export const getAvgDuration = async (jobId: string): Promise<AvgDuration> => {
  if (USE_MOCK_DATA) {
    return computeAvgDuration(sampleRuns);
  }
  const response = await apiClient.get<AvgDuration>(
    `${base(jobId)}/avg-duration`,
  );
  return response.data;
};

export const getLastRun = async (jobId: string): Promise<JobRun | null> => {
  if (USE_MOCK_DATA) {
    return sampleRuns[0] ?? null;
  }
  const response = await apiClient.get<JobRun | null>(`${base(jobId)}/last`);
  return response.data;
};

export type SortDir = 'asc' | 'desc';
export type RunSortField = keyof JobRun;

export type JobRunPage = {
  content: JobRun[];
  page: number;
  size: number;
  totalElements: number;
};

export const getRuns = async (
  jobId: string,
  sortBy: RunSortField = 'executionId',
  sortDir: SortDir = 'desc',
  page = 0,
  size = 20,
): Promise<JobRunPage> => {
  if (USE_MOCK_DATA) {
    const sorted = [...sampleRuns].sort((a, b) => {
      const av = a[sortBy];
      const bv = b[sortBy];
      if (av === bv) {
        return 0;
      }
      if (av === null || av === undefined) {
        return 1;
      }
      if (bv === null || bv === undefined) {
        return -1;
      }
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
  const response = await apiClient.get<JobRunPage>(base(jobId), {
    params: { sortBy, sortDir, page, size },
  });
  return response.data;
};

export const getRunsTrend = async (
  jobId: string,
  windowDays: number,
): Promise<JobRun[]> => {
  if (USE_MOCK_DATA) {
    return filterTrendRuns(sampleRuns, windowDays);
  }
  const response = await apiClient.get<JobRun[]>(`${base(jobId)}/trend`, {
    params: { window: windowDays },
  });
  return response.data;
};
