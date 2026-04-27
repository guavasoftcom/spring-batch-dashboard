import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';

const mockJobs = [
  'reconcileLedgerJob',
  'importCustomersJob',
  'archiveOrdersJob',
  'syncInventoryJob',
];

export const getJobs = async (): Promise<string[]> => {
  if (USE_MOCK_DATA) {
    return [...mockJobs];
  }
  const response = await apiClient.get<string[]>('/api/jobs');
  return response.data;
};
