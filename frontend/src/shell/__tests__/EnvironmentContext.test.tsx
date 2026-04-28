import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { EnvironmentContext, useEnvironment } from '~/shell/EnvironmentContext';

const Probe = () => {
  const { environment } = useEnvironment();
  return <p>env={environment || 'none'}</p>;
};

describe('EnvironmentContext', () => {
  it('exposes the default empty value when no provider wraps the consumer', () => {
    render(<Probe />);
    expect(screen.getByText('env=none')).toBeInTheDocument();
  });

  it('lets a provider override the value seen by consumers', () => {
    render(
      <EnvironmentContext.Provider value={{ environment: 'staging', setEnvironment: () => {} }}>
        <Probe />
      </EnvironmentContext.Provider>,
    );
    expect(screen.getByText('env=staging')).toBeInTheDocument();
  });
});
