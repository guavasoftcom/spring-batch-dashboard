import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';

const mockJobs = [
  'reconcileLedgerJob',
  'importCustomersJob',
  'archiveOrdersJob',
  'syncInventoryJob',
];

/**
 * Names of jobs with at least one run in the last `windowDays` days. Mock mode returns every
 * seed job regardless of the window.
 */
export const getJobs = async (windowDays: number): Promise<string[]> => {
  if (USE_MOCK_DATA) {
    return [...mockJobs];
  }
  const response = await apiClient.get<string[]>('/api/jobs', { params: { window: windowDays } });
  return response.data;
};
