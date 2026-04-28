import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, RenderOptions } from '@testing-library/react';
import { ReactElement, ReactNode } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { EnvironmentContext } from '~/shell/EnvironmentContext';

type Options = Omit<RenderOptions, 'wrapper'> & {
  initialEntries?: string[];
  routePath?: string;
  environment?: string;
  setEnvironment?: (value: string) => void;
};

/**
 * Renders a component tree with the providers every authenticated tile expects:
 * a fresh QueryClient (no retries, no cache bleed), the EnvironmentContext, and a
 * MemoryRouter. Pass `routePath` to mount the children under a specific Routes
 * pattern (e.g. '/jobs/:jobId') so useParams resolves.
 */
export const renderWithProviders = (
  ui: ReactElement,
  {
    initialEntries = ['/'],
    routePath,
    environment = 'prod',
    setEnvironment = () => {},
    ...renderOptions
  }: Options = {},
) => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  const Wrapper = ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <EnvironmentContext.Provider value={{ environment, setEnvironment }}>
        <MemoryRouter initialEntries={initialEntries}>
          {routePath ? (
            <Routes>
              <Route path={routePath} element={<>{children}</>} />
              <Route path="*" element={<>{children}</>} />
            </Routes>
          ) : (
            children
          )}
        </MemoryRouter>
      </EnvironmentContext.Provider>
    </QueryClientProvider>
  );

  return { ...render(ui, { wrapper: Wrapper, ...renderOptions }), queryClient };
};
