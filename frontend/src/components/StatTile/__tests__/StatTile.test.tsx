import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import StatTile from '~/components/StatTile/StatTile';

describe('StatTile', () => {
  it('renders the title and value when not loading', () => {
    render(<StatTile title="Total Runs" value={42} subtitle="across last week" />);

    expect(screen.getByText('Total Runs')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
    expect(screen.getByText('across last week')).toBeInTheDocument();
  });

  it('shows loading skeletons in place of the value when loading', () => {
    render(<StatTile title="Total Runs" loading />);

    expect(screen.getByText('Total Runs')).toBeInTheDocument();
    expect(screen.queryByText('42')).not.toBeInTheDocument();
  });

  it('shows the error message and hides the value when error is set', () => {
    render(<StatTile title="Total Runs" value={42} error="Failed to load" />);

    expect(screen.getByText('Failed to load')).toBeInTheDocument();
    expect(screen.queryByText('42')).not.toBeInTheDocument();
  });

  it('renders the empty placeholder when value is undefined and not loading or errored', () => {
    render(<StatTile title="Total Runs" empty="No data yet" />);

    expect(screen.getByText('No data yet')).toBeInTheDocument();
  });

  it('omits the subtitle when value is null even if a subtitle is provided', () => {
    render(<StatTile title="Total Runs" value={null} subtitle="never shown" />);

    expect(screen.queryByText('never shown')).not.toBeInTheDocument();
  });
});
