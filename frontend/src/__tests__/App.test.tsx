import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '~/App';

describe('App', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders heading', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: false } as Response);

    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>,
    );

    expect(
      await screen.findByRole('heading', { level: 1, name: /spring batch\s+dashboard/i }),
    ).toBeInTheDocument();
  });
});
