import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import LoginPage from '~/pages/login/LoginPage';
import { ColorModeProvider } from '~/theme';
import type { OAuth2Provider } from '~/types';

const renderInProvider = (ui: React.ReactElement) =>
  render(<ColorModeProvider>{ui}</ColorModeProvider>);

const githubProvider: OAuth2Provider = {
  id: 'github',
  label: 'GitHub',
  loginUrl: '/oauth2/authorization/github',
  color: '#24292e',
  iconUrl: null,
};

describe('LoginPage', () => {
  it('renders the welcome title and the configured sign-in buttons when not checking', () => {
    renderInProvider(<LoginPage checking={false} providers={[githubProvider]} />);

    expect(screen.getAllByText('Welcome back!').length).toBeGreaterThan(0);
    const signIn = screen.getByRole('link', { name: /sign in with github/i });
    expect(signIn).toBeInTheDocument();
    expect(signIn).toHaveAttribute('href', expect.stringContaining(githubProvider.loginUrl));
  });

  it('shows a loading message and hides the buttons while checking', () => {
    renderInProvider(<LoginPage checking providers={[githubProvider]} />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /sign in with github/i })).not.toBeInTheDocument();
  });

  it('renders both the desktop and compact title blocks', () => {
    renderInProvider(<LoginPage checking={false} providers={[githubProvider]} />);

    // The compact title is rendered alongside the decorative panel and hidden via CSS on md+;
    // both copies live in the DOM, so we expect two of each title fragment.
    expect(screen.getAllByText('Welcome back!')).toHaveLength(2);
    expect(screen.getAllByText('Spring Batch')).toHaveLength(2);
    expect(screen.getAllByText('Dashboard')).toHaveLength(2);
  });

  it('renders the lock heading and divider above the sign-in button', () => {
    const { container } = renderInProvider(
      <LoginPage checking={false} providers={[githubProvider]} />,
    );

    expect(screen.getByRole('heading', { level: 2, name: 'Login' })).toBeInTheDocument();
    expect(container.querySelector('[data-testid="LockOutlinedIcon"]')).toBeInTheDocument();
  });

  it('renders an empty-state message when no providers are configured', () => {
    renderInProvider(<LoginPage checking={false} providers={[]} />);

    expect(screen.getByText(/no sign-in providers are configured/i)).toBeInTheDocument();
    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });

  it('renders an icon when iconUrl is provided', () => {
    const withIcon: OAuth2Provider = {
      ...githubProvider,
      iconUrl: 'data:image/svg+xml;base64,PHN2Zy8+',
    };
    const { container } = renderInProvider(<LoginPage checking={false} providers={[withIcon]} />);

    const img = container.querySelector('img');
    expect(img).not.toBeNull();
    expect(img?.getAttribute('src')).toBe('data:image/svg+xml;base64,PHN2Zy8+');
  });

  it('mounts cleanly in dark mode (drives the dark-branch styling)', () => {
    window.localStorage.setItem('spring-batch-dashboard.colorMode', 'dark');
    try {
      renderInProvider(<LoginPage checking={false} providers={[githubProvider]} />);
      expect(screen.getByRole('link', { name: /sign in with github/i })).toBeInTheDocument();
    } finally {
      window.localStorage.clear();
    }
  });
});
