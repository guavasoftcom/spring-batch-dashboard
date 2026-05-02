import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import ExecutionLink from '~/components/ExecutionLink/ExecutionLink';

describe('ExecutionLink', () => {
  it('renders the executionId prefixed with #', () => {
    render(<ExecutionLink executionId={42} onClick={() => {}} />);
    expect(screen.getByRole('button')).toHaveTextContent('#42');
  });

  it('renders a DataThresholding icon to the left of the number', () => {
    const { container } = render(<ExecutionLink executionId={1} onClick={() => {}} />);
    const icon = container.querySelector('svg[data-testid="DataThresholdingIcon"]');
    expect(icon).toBeInTheDocument();
  });

  it('invokes onClick with the executionId when clicked', async () => {
    const user = userEvent.setup();
    const handle = vi.fn();
    render(<ExecutionLink executionId={7} onClick={handle} />);

    await user.click(screen.getByRole('button'));

    expect(handle).toHaveBeenCalledWith(7);
  });

  it('uses small icon styling for the default cell variant', () => {
    const { container } = render(<ExecutionLink executionId={1} onClick={() => {}} />);
    const icon = container.querySelector('svg[data-testid="DataThresholdingIcon"]');
    expect(icon?.classList.contains('MuiSvgIcon-fontSizeSmall')).toBe(true);
  });

  it('uses larger fontSize for the large variant', () => {
    const { container } = render(
      <ExecutionLink executionId={1} onClick={() => {}} variant="large" />,
    );
    const icon = container.querySelector('svg[data-testid="DataThresholdingIcon"]') as SVGElement;
    expect(icon?.classList.contains('MuiSvgIcon-fontSizeSmall')).toBe(false);
    const link = screen.getByRole('button');
    expect(window.getComputedStyle(link).fontSize).toBe('2.125rem');
  });

  it('merges caller-provided sx', () => {
    render(<ExecutionLink executionId={1} onClick={() => {}} sx={{ opacity: 0.5 }} />);
    const link = screen.getByRole('button');
    expect(window.getComputedStyle(link).opacity).toBe('0.5');
  });
});
