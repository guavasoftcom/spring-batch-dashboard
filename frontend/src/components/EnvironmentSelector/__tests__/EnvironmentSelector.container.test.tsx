import { screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EnvironmentSelectorContainer from '~/components/EnvironmentSelector/EnvironmentSelector.container';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMocks = vi.hoisted(() => ({ getEnvironments: vi.fn() }));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMocks };
});

describe('EnvironmentSelector container', () => {
  beforeEach(() => {
    apiMocks.getEnvironments.mockResolvedValue([
      { name: 'dev', type: 'POSTGRESQL' },
      { name: 'prod', type: 'POSTGRESQL' },
      { name: 'staging', type: 'MYSQL' },
    ]);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the selector once multiple environments are available', async () => {
    renderWithProviders(<EnvironmentSelectorContainer />, { environment: 'prod' });

    await waitFor(() => expect(screen.getByRole('combobox')).toBeInTheDocument());
    expect(screen.getByRole('combobox')).toHaveTextContent('prod');
  });

  it('falls back to the first environment when the current one is not in the option list', async () => {
    const setEnvironment = vi.fn();
    renderWithProviders(<EnvironmentSelectorContainer />, {
      environment: 'ghost',
      setEnvironment,
    });

    await waitFor(() => expect(setEnvironment).toHaveBeenCalledWith('dev'));
  });

  it('renders nothing when only one environment is available', async () => {
    apiMocks.getEnvironments.mockResolvedValueOnce([{ name: 'only-one', type: 'POSTGRESQL' }]);

    const { container } = renderWithProviders(<EnvironmentSelectorContainer />);

    await waitFor(() => expect(container.querySelector('[role="combobox"]')).not.toBeInTheDocument());
  });
});
