import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { ReactNode } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { useEnvQuery, useExecutionQuery, useJobQuery } from '~/hooks/useTileQuery';
import { EnvironmentContext } from '~/shell/EnvironmentContext';

const buildWrapper = (
  environment: string,
  jobIdRoute?: { path: string; jobId: string },
) => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={client}>
      <EnvironmentContext.Provider value={{ environment, setEnvironment: () => {} }}>
        {jobIdRoute ? (
          <MemoryRouter initialEntries={[jobIdRoute.path]}>
            <Routes>
              <Route path="/jobs/:jobId" element={<>{children}</>} />
            </Routes>
          </MemoryRouter>
        ) : (
          <MemoryRouter>{children}</MemoryRouter>
        )}
      </EnvironmentContext.Provider>
    </QueryClientProvider>
  );
};

describe('useEnvQuery', () => {
  it('runs the query when environment is set and reports loading then success', async () => {
    const fetcher = vi.fn().mockResolvedValue({ value: 42 });
    const wrapper = buildWrapper('prod');

    const { result } = renderHook(() => useEnvQuery(['key'], fetcher), { wrapper });

    expect(result.current.loading).toBe(true);
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toEqual({ value: 42 });
    expect(result.current.error).toBeNull();
    expect(fetcher).toHaveBeenCalledOnce();
  });

  it('stays disabled when environment is empty', () => {
    const fetcher = vi.fn();
    const wrapper = buildWrapper('');

    const { result } = renderHook(() => useEnvQuery(['key'], fetcher), { wrapper });

    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBeNull();
    expect(fetcher).not.toHaveBeenCalled();
  });

  it('surfaces errors as a generic Failed to load message', async () => {
    const fetcher = vi.fn().mockRejectedValue(new Error('boom'));
    const wrapper = buildWrapper('prod');

    const { result } = renderHook(() => useEnvQuery(['key'], fetcher), { wrapper });

    await waitFor(() => expect(result.current.error).toBe('Failed to load'));
    expect(result.current.data).toBeNull();
  });
});

describe('useJobQuery', () => {
  it('passes the route :jobId param to the fetcher', async () => {
    const fetcher = vi.fn(async (jobId: string) => ({ jobId }));
    const wrapper = buildWrapper('prod', { path: '/jobs/abc-123', jobId: 'abc-123' });

    const { result } = renderHook(() => useJobQuery(['runs'], fetcher), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(fetcher).toHaveBeenCalledWith('abc-123');
    expect(result.current.data).toEqual({ jobId: 'abc-123' });
  });
});

describe('useExecutionQuery', () => {
  it('passes the executionId argument to the fetcher and returns data', async () => {
    const fetcher = vi.fn(async (executionId: string) => ({ executionId }));
    const wrapper = buildWrapper('prod');

    const { result } = renderHook(() => useExecutionQuery('99', ['steps'], fetcher), { wrapper });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(fetcher).toHaveBeenCalledWith('99');
    expect(result.current.data).toEqual({ executionId: '99' });
  });

  it('stays disabled until executionId is provided', () => {
    const fetcher = vi.fn();
    const wrapper = buildWrapper('prod');

    const { result } = renderHook(() => useExecutionQuery(undefined, ['steps'], fetcher), { wrapper });

    expect(result.current.loading).toBe(true);
    expect(fetcher).not.toHaveBeenCalled();
  });
});
