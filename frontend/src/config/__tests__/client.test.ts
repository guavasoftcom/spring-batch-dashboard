import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '~/config/client';

const ENV_STORAGE_KEY = 'spring-batch-dashboard.environment';

describe('apiClient request interceptor', () => {
  afterEach(() => {
    window.localStorage.clear();
  });

  it('attaches X-Environment header when an environment is stored', async () => {
    window.localStorage.setItem(ENV_STORAGE_KEY, 'prod');

    const handlers = (apiClient.interceptors.request as unknown as { handlers: Array<{ fulfilled: (config: unknown) => unknown }> }).handlers;
    const config = { headers: new Map() };
    config.headers.set = vi.fn();
    await handlers[0].fulfilled(config);

    expect(config.headers.set).toHaveBeenCalledWith('X-Environment', 'prod');
  });

  it('does not set the header when no environment is stored', async () => {
    const handlers = (apiClient.interceptors.request as unknown as { handlers: Array<{ fulfilled: (config: unknown) => unknown }> }).handlers;
    const config = { headers: new Map() };
    config.headers.set = vi.fn();
    await handlers[0].fulfilled(config);

    expect(config.headers.set).not.toHaveBeenCalled();
  });
});

describe('apiClient response interceptor', () => {
  let originalLocation: Location;

  beforeEach(() => {
    originalLocation = window.location;
    Object.defineProperty(window, 'location', {
      writable: true,
      value: { pathname: '/overview', replace: vi.fn() },
    });
  });

  afterEach(() => {
    Object.defineProperty(window, 'location', { writable: true, value: originalLocation });
  });

  const errorHandler = () => {
    type Handlers = { handlers: Array<{ rejected: (error: unknown) => Promise<unknown> }> };
    return (apiClient.interceptors.response as unknown as Handlers).handlers[0].rejected;
  };

  it('redirects to / when a 401 response arrives outside the login route', async () => {
    await errorHandler()({ response: { status: 401 } }).catch(() => {});

    expect((window.location.replace as unknown as ReturnType<typeof vi.fn>)).toHaveBeenCalledWith('/');
  });

  it('does not redirect when already on the login route', async () => {
    (window.location as unknown as { pathname: string }).pathname = '/';

    await errorHandler()({ response: { status: 401 } }).catch(() => {});

    expect((window.location.replace as unknown as ReturnType<typeof vi.fn>)).not.toHaveBeenCalled();
  });

  it('does not redirect on non-401 errors', async () => {
    await errorHandler()({ response: { status: 500 } }).catch(() => {});

    expect((window.location.replace as unknown as ReturnType<typeof vi.fn>)).not.toHaveBeenCalled();
  });

  it('rejects with the original error', async () => {
    const error = { response: { status: 500 } };
    await expect(errorHandler()(error)).rejects.toBe(error);
  });
});
