import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const mockState = { useMock: false };

vi.mock( '~/config/env', () => ({
  get USE_MOCK_DATA() {
    return mockState.useMock;
  },
}));

vi.mock( '~/config/client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import { apiClient } from '~/config/client';
import { getCurrentUser, logout } from '~/api/authApi';

describe('authApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('real mode', () => {
    it('getCurrentUser hits /api/me', async () => {
      const user = { login: 'guad', name: 'Guad', avatarUrl: null };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: user });

      await expect(getCurrentUser()).resolves.toEqual(user);
      expect(apiClient.get).toHaveBeenCalledWith('/api/auth/me');
    });

    it('logout posts /api/logout', async () => {
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: undefined });

      await logout();

      expect(apiClient.post).toHaveBeenCalledWith('/api/logout');
    });
  });

  describe('mock mode', () => {
    beforeEach(() => {
      mockState.useMock = true;
    });

    it('getCurrentUser returns canned mock user without HTTP call', async () => {
      const result = await getCurrentUser();

      expect(result).toEqual({ login: 'mock-user', name: 'Mock User', avatarUrl: null });
      expect(apiClient.get).not.toHaveBeenCalled();
    });

    it('logout no-ops without HTTP call', async () => {
      await logout();

      expect(apiClient.post).not.toHaveBeenCalled();
    });
  });
});
