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
import { getJobs } from '~/api/jobsApi';

describe('jobsApi', () => {
  beforeEach(() => {
    mockState.useMock = false;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('getJobs hits /api/jobs in real mode', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce({ data: ['jobA', 'jobB'] });

    await expect(getJobs()).resolves.toEqual(['jobA', 'jobB']);
    expect(apiClient.get).toHaveBeenCalledWith('/api/jobs');
  });

  it('getJobs returns mock jobs in mock mode', async () => {
    mockState.useMock = true;

    const result = await getJobs();

    expect(result).toEqual([
      'reconcileLedgerJob',
      'importCustomersJob',
      'archiveOrdersJob',
      'syncInventoryJob',
    ]);
    expect(apiClient.get).not.toHaveBeenCalled();
  });
});
