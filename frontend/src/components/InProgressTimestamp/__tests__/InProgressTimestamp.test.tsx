import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import InProgressTimestamp from '~/components/InProgressTimestamp/InProgressTimestamp';
import { formatTimestamp } from '~/utils';

describe('InProgressTimestamp', () => {
  it('renders the formatted timestamp when value is provided', () => {
    const value = '2026-01-15T14:30:00Z';
    render(<InProgressTimestamp value={value} />);
    expect(screen.getByText(formatTimestamp(value))).toBeInTheDocument();
  });

  it('renders an in-progress indicator when value is null', () => {
    render(<InProgressTimestamp value={null} />);
    expect(screen.getByText('In progress')).toBeInTheDocument();
  });

  it('does not render the in-progress indicator when value is provided', () => {
    render(<InProgressTimestamp value="2026-01-15T14:30:00Z" />);
    expect(screen.queryByText('In progress')).not.toBeInTheDocument();
  });
});
