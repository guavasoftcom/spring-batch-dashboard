import { afterEach, describe, expect, it, vi } from 'vitest';

const importEnvModule = async () => {
  vi.resetModules();
  return import('~/config/env');
};

describe('BACKEND_BASE_URL', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
  });

  // Guards the released image: the JAR serves the SPA and the API from the same
  // origin, so an unset VITE_BACKEND_BASE_URL must stay empty. A hardcoded
  // localhost fallback here would be baked into the published bundle.
  it('falls back to the current origin when unset', async () => {
    vi.stubEnv('VITE_BACKEND_BASE_URL', undefined);

    const { BACKEND_BASE_URL } = await importEnvModule();

    expect(BACKEND_BASE_URL).toBe('');
  });

  it('uses the configured base URL when one is set', async () => {
    vi.stubEnv('VITE_BACKEND_BASE_URL', 'https://dashboard.example.com');

    const { BACKEND_BASE_URL } = await importEnvModule();

    expect(BACKEND_BASE_URL).toBe('https://dashboard.example.com');
  });
});
