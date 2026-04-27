import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';

const mockEnvironments = ['Dev Datamart', 'Dev ETL', 'Test Datamart', 'Test ETL'];

export const getEnvironments = async (): Promise<string[]> => {
  if (USE_MOCK_DATA) {
    return [...mockEnvironments];
  }
  const response = await apiClient.get<string[]>('/api/environments');
  return response.data;
};
