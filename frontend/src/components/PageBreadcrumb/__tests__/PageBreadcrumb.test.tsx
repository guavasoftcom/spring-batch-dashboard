import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import PageBreadcrumb from '~/components/PageBreadcrumb/PageBreadcrumb';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, getEnvironments: vi.fn().mockResolvedValue([]) };
});

describe('PageBreadcrumb', () => {
  it('renders all segments with humanized labels', () => {
    renderWithProviders(
      <PageBreadcrumb
        segments={[
          { label: 'prod' },
          { label: 'importUsersJob' },
          { label: 'execution-7' },
        ]}
      />,
    );

    expect(screen.getByText('Prod')).toBeInTheDocument();
    expect(screen.getByText('Import Users Job')).toBeInTheDocument();
    expect(screen.getByText(/Execution[\s-]?7/)).toBeInTheDocument();
  });

  it('renders clickable segments as buttons that fire onClick', async () => {
    const user = userEvent.setup();
    const onJob = vi.fn();
    renderWithProviders(
      <PageBreadcrumb
        segments={[
          { label: 'prod' },
          { label: 'importUsersJob', onClick: onJob },
          { label: 'execution-7' },
        ]}
      />,
    );

    await user.click(screen.getByRole('button', { name: 'Import Users Job' }));

    expect(onJob).toHaveBeenCalledOnce();
  });

  it('renders a single segment without a chevron separator', () => {
    const { container } = renderWithProviders(<PageBreadcrumb segments={[{ label: 'prod' }]} />);

    expect(container.querySelectorAll('svg').length).toBe(0);
  });

  it('inserts a chevron between consecutive segments', () => {
    const { container } = renderWithProviders(
      <PageBreadcrumb segments={[{ label: 'prod' }, { label: 'importUsersJob' }]} />,
    );

    expect(container.querySelectorAll('svg').length).toBe(1);
  });
});
