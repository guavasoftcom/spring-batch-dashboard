import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import LargeTile from '~/components/LargeTile/LargeTile';

describe('LargeTile', () => {
  it('renders title and children when not loading or errored', () => {
    render(
      <LargeTile title="Job Status">
        <p>chart goes here</p>
      </LargeTile>,
    );

    expect(screen.getByRole('heading', { name: 'Job Status' })).toBeInTheDocument();
    expect(screen.getByText('chart goes here')).toBeInTheDocument();
  });

  it('renders the headerAction next to the title', () => {
    render(
      <LargeTile title="Job Runs" headerAction={<button>Refresh</button>}>
        <p>body</p>
      </LargeTile>,
    );

    expect(screen.getByRole('button', { name: 'Refresh' })).toBeInTheDocument();
  });

  it('shows the default skeleton when loading and hides children', () => {
    render(
      <LargeTile title="Job Status" loading>
        <p>should not render</p>
      </LargeTile>,
    );

    expect(screen.queryByText('should not render')).not.toBeInTheDocument();
  });

  it('renders a custom loadingSkeleton when supplied', () => {
    render(
      <LargeTile title="Job Status" loading loadingSkeleton={<p>custom skeleton</p>}>
        <p>should not render</p>
      </LargeTile>,
    );

    expect(screen.getByText('custom skeleton')).toBeInTheDocument();
    expect(screen.queryByText('should not render')).not.toBeInTheDocument();
  });

  it('shows the error message and hides children when errored', () => {
    render(
      <LargeTile title="Job Status" error="boom">
        <p>should not render</p>
      </LargeTile>,
    );

    expect(screen.getByText('boom')).toBeInTheDocument();
    expect(screen.queryByText('should not render')).not.toBeInTheDocument();
  });
});
