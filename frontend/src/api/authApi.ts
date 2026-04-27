import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import type { CurrentUserResponse } from '~/types';

const mockUser: CurrentUserResponse = {
  login: 'mock-user',
  name: 'Mock User',
  avatarUrl: null,
};

export const getCurrentUser = async (): Promise<CurrentUserResponse> => {
  if (USE_MOCK_DATA) {
    return mockUser;
  }
  const response = await apiClient.get<CurrentUserResponse>('/api/auth/me');
  return response.data;
};

export const logout = async (): Promise<void> => {
  if (USE_MOCK_DATA) {
    return;
  }
  await apiClient.post('/api/logout');
};
