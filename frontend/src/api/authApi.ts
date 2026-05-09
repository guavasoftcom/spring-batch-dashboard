import { apiClient } from '~/config/client';
import { USE_MOCK_DATA } from '~/config/env';
import type { CurrentUserResponse, OAuth2Provider } from '~/types';

const mockUser: CurrentUserResponse = {
  login: 'mock-user',
  name: 'Mock User',
  avatarUrl: null,
};

const mockProviders: OAuth2Provider[] = [
  {
    id: 'github',
    label: 'GitHub',
    loginUrl: '/oauth2/authorization/github',
    color: '#24292e',
    iconUrl: null,
  },
];

export const getCurrentUser = async (): Promise<CurrentUserResponse> => {
  if (USE_MOCK_DATA) {
    return mockUser;
  }
  const response = await apiClient.get<CurrentUserResponse>('/api/auth/me');
  return response.data;
};

export const getOAuth2Providers = async (): Promise<OAuth2Provider[]> => {
  if (USE_MOCK_DATA) {
    return mockProviders;
  }
  const response = await apiClient.get<OAuth2Provider[]>('/api/auth/providers');
  return response.data;
};

export const logout = async (): Promise<void> => {
  if (USE_MOCK_DATA) {
    return;
  }
  await apiClient.post('/api/logout');
};
