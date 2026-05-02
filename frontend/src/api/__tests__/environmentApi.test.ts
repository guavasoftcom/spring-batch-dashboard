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

const expectedMockEnvironments = [
  { name: 'Localhost Warehouse', type: 'POSTGRESQL' },
  { name: 'Test Warehouse', type: 'MYSQL' },
  { name: 'Prod Warehouse', type: 'ORACLE' },
];

describe('environmentApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('getEnvironments hits /api/environments in real mode', async () => {
    const payload = [
      { name: 'prod', type: 'POSTGRESQL' },
      { name: 'staging', type: 'MYSQL' },
    ];
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: payload });

    await expect(getEnvironments()).resolves.toEqual(payload);
    expect(apiClient.get).toHaveBeenCalledWith('/api/environments');
  });

  it('getEnvironments returns mock list copy in mock mode', async () => {
    mockState.useMock = true;

    const result = await getEnvironments();

    expect(result).toEqual(expectedMockEnvironments);
    expect(apiClient.get).not.toHaveBeenCalled();
  });
});
