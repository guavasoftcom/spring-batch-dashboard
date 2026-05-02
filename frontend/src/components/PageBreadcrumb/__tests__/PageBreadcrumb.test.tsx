import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import PageBreadcrumb from '~/components/PageBreadcrumb/PageBreadcrumb';
import { NavContext } from '~/shell/NavContext';
import { renderWithProviders } from '~/test-utils/renderWithProviders';

const apiMock = vi.hoisted(() => ({ getEnvironments: vi.fn() }));

vi.mock('~/api', async () => {
  const actual = await vi.importActual<object>('~/api');
  return { ...actual, ...apiMock };
});

apiMock.getEnvironments.mockResolvedValue([]);

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

  it('prepends the active environment when the side nav is hidden', async () => {
    apiMock.getEnvironments.mockResolvedValueOnce([
      { name: 'prod', type: 'POSTGRESQL' },
    ]);

    renderWithProviders(
      <NavContext.Provider value={{ navOpen: false, setNavOpen: () => {} }}>
        <PageBreadcrumb segments={[{ label: 'Overview' }]} />
      </NavContext.Provider>,
      { environment: 'prod' },
    );

    await waitFor(() => expect(screen.getByText('Prod')).toBeInTheDocument());
    expect(screen.getByText('Overview')).toBeInTheDocument();
  });

  it('does not prepend an env segment when the nav is open', () => {
    renderWithProviders(
      <NavContext.Provider value={{ navOpen: true, setNavOpen: () => {} }}>
        <PageBreadcrumb segments={[{ label: 'Overview' }]} />
      </NavContext.Provider>,
      { environment: 'prod' },
    );

    expect(screen.queryByText('Prod')).not.toBeInTheDocument();
  });
});
