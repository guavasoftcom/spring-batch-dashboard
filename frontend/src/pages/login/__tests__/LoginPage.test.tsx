import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import LoginPage from '~/pages/login/LoginPage';
import { ColorModeProvider } from '~/theme';

const renderInProvider = (ui: React.ReactElement) =>
  render(<ColorModeProvider>{ui}</ColorModeProvider>);

describe('LoginPage', () => {
  it('renders the welcome title and the GitHub sign-in button when not checking', () => {
    renderInProvider(<LoginPage checking={false} loginUrl="/oauth2/start" />);

    expect(screen.getAllByText('Welcome back!').length).toBeGreaterThan(0);
    const signIn = screen.getByRole('link', { name: /sign in with github/i });
    expect(signIn).toBeInTheDocument();
    expect(signIn).toHaveAttribute('href', '/oauth2/start');
  });

  it('shows a loading message and hides the button while checking', () => {
    renderInProvider(<LoginPage checking loginUrl="/oauth2/start" />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /sign in with github/i })).not.toBeInTheDocument();
  });

  it('renders both the desktop and compact title blocks', () => {
    renderInProvider(<LoginPage checking={false} loginUrl="/oauth2/start" />);

    // The compact title is rendered alongside the decorative panel and hidden via CSS on md+;
    // both copies live in the DOM, so we expect two of each title fragment.
    expect(screen.getAllByText('Welcome back!')).toHaveLength(2);
    expect(screen.getAllByText('Spring Batch')).toHaveLength(2);
    expect(screen.getAllByText('Dashboard')).toHaveLength(2);
  });

  it('renders the lock heading and divider above the sign-in button', () => {
    const { container } = renderInProvider(
      <LoginPage checking={false} loginUrl="/oauth2/start" />,
    );

    expect(screen.getByRole('heading', { level: 2, name: 'Login' })).toBeInTheDocument();
    expect(container.querySelector('[data-testid="LockOutlinedIcon"]')).toBeInTheDocument();
  });

  it('mounts cleanly in dark mode (drives the dark-branch styling)', () => {
    window.localStorage.setItem('spring-batch-dashboard.colorMode', 'dark');
    try {
      renderInProvider(<LoginPage checking={false} loginUrl="/oauth2/start" />);
      expect(screen.getByRole('link', { name: /sign in with github/i })).toBeInTheDocument();
    } finally {
      window.localStorage.clear();
    }
  });
});
