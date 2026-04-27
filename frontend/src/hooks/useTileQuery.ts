import { useQuery, type UseQueryOptions } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import { useEnvironment } from '~/shell/EnvironmentContext';

export type TileQueryResult<T> = {
  data: T | null;
  loading: boolean;
  error: string | null;
};

type ExtraOptions<T> = Pick<UseQueryOptions<T>, 'staleTime' | 'refetchOnWindowFocus' | 'placeholderData'>;

const toResult = <T>(q: { data: T | undefined; isPending: boolean; error: unknown }): TileQueryResult<T> => ({
  data: q.data ?? null,
  loading: q.isPending,
  error: q.error ? 'Failed to load' : null,
});

/**
 * Query keyed by the active environment. Use for environment-scoped tiles
 * that don't depend on a path/url parameter.
 */
export const useEnvQuery = <T>(
  key: readonly unknown[],
  fn: () => Promise<T>,
  options?: ExtraOptions<T>,
): TileQueryResult<T> => {
  const { environment } = useEnvironment();
  return toResult(useQuery({
    queryKey: [...key, environment],
    queryFn: fn,
    enabled: !!environment,
    ...options,
  }));
};

/**
 * Query keyed by environment + the current `:jobId` route param. Gates on a
 * truthy jobId so the query stays disabled until the route is mounted.
 */
export const useJobQuery = <T>(
  key: readonly unknown[],
  fn: (jobId: string) => Promise<T>,
  options?: ExtraOptions<T>,
): TileQueryResult<T> => {
  const { environment } = useEnvironment();
  const { jobId } = useParams<{ jobId: string }>();
  return toResult(useQuery({
    queryKey: [...key, environment, jobId],
    queryFn: () => fn(jobId!),
    enabled: !!jobId && !!environment,
    ...options,
  }));
};

/**
 * Query keyed by environment + the supplied executionId. Gates on a truthy
 * executionId so the query stays disabled until the parent supplies one.
 */
export const useExecutionQuery = <T>(
  executionId: string | undefined,
  key: readonly unknown[],
  fn: (executionId: string) => Promise<T>,
  options?: ExtraOptions<T>,
): TileQueryResult<T> => {
  const { environment } = useEnvironment();
  return toResult(useQuery({
    queryKey: [...key, environment, executionId],
    queryFn: () => fn(executionId!),
    enabled: !!executionId && !!environment,
    ...options,
  }));
};
