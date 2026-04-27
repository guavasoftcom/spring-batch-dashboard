import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const mockState = { useMock: false };

vi.mock( '~/config/env', () => ({
  get USE_MOCK_DATA() {
    return mockState.useMock;
  },
}));

vi.mock( '~/config/client', () => ({
  apiClient: { get: vi.fn() },
}));

import { apiClient } from '~/config/client';
import { getEnvironments } from '~/api/environmentApi';

const expectedMockEnvironments = ['Dev Datamart', 'Dev ETL', 'Test Datamart', 'Test ETL'];

describe('environmentApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('getEnvironments hits /api/environments in real mode', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: ['prod', 'staging'] });

    await expect(getEnvironments()).resolves.toEqual(['prod', 'staging']);
    expect(apiClient.get).toHaveBeenCalledWith('/api/environments');
  });

  it('getEnvironments returns mock list copy in mock mode', async () => {
    mockState.useMock = true;

    const result = await getEnvironments();

    expect(result).toEqual(expectedMockEnvironments);
    expect(apiClient.get).not.toHaveBeenCalled();
  });
});
