import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import type { EnvironmentInfo } from '~/types';

const mockEnvironments: EnvironmentInfo[] = [
  { name: 'Localhost Warehouse', type: 'POSTGRESQL' },
  { name: 'Test Warehouse', type: 'MYSQL' },
  { name: 'Prod Warehouse', type: 'ORACLE' },
];

export const getEnvironments = async (): Promise<EnvironmentInfo[]> => {
  if (USE_MOCK_DATA) {
    return mockEnvironments.map((env) => ({ ...env }));
  }
  const response = await apiClient.get<EnvironmentInfo[]>('/api/environments');
  return response.data;
};
