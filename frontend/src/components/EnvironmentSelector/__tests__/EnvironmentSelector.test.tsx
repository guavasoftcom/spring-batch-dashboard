import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import EnvironmentSelector from '~/components/EnvironmentSelector/EnvironmentSelector';
import type { EnvironmentInfo } from '~/types';

const sampleOptions: EnvironmentInfo[] = [
  { name: 'dev', type: 'POSTGRESQL' },
  { name: 'prod', type: 'MYSQL' },
];

describe('EnvironmentSelector', () => {
  it('renders the selected option as the value', () => {
    render(
      <EnvironmentSelector value="prod" selectedType="MYSQL" options={sampleOptions} onChange={() => {}} />,
    );

    expect(screen.getByRole('combobox')).toHaveTextContent('prod');
  });

  it('renders a skeleton instead of the select when loading', () => {
    render(
      <EnvironmentSelector value="prod" selectedType="MYSQL" options={sampleOptions} onChange={() => {}} loading />,
    );

    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
  });

  it('disables the control when no options are available', () => {
    render(<EnvironmentSelector value="" selectedType="" options={[]} onChange={() => {}} />);

    expect(screen.getByRole('combobox')).toHaveAttribute('aria-disabled', 'true');
  });

  it('shows a tooltip with the full environment name when hovering the trigger', async () => {
    const longName = 'A Really Long Environment Name That Definitely Overflows';
    render(
      <EnvironmentSelector
        value={longName}
        selectedType="POSTGRESQL"
        options={[{ name: longName, type: 'POSTGRESQL' }]}
        onChange={() => {}}
      />,
    );

    fireEvent.mouseEnter(screen.getByText(longName));

    expect(await screen.findByRole('tooltip')).toHaveTextContent(longName);
  });

  it('truncates the trigger text with ellipsis styling', () => {
    const longName = 'A Really Long Environment Name That Definitely Overflows';
    render(
      <EnvironmentSelector
        value={longName}
        selectedType="POSTGRESQL"
        options={[{ name: longName, type: 'POSTGRESQL' }]}
        onChange={() => {}}
      />,
    );

    const label = screen.getByText(longName);
    const styles = window.getComputedStyle(label);
    expect(styles.textOverflow).toBe('ellipsis');
    expect(styles.whiteSpace).toBe('nowrap');
    expect(styles.overflow).toBe('hidden');
  });

  it('invokes onChange with the picked value', async () => {
    const user = userEvent.setup();
    const handle = vi.fn();
    render(
      <EnvironmentSelector value="dev" selectedType="POSTGRESQL" options={sampleOptions} onChange={handle} />,
    );

    await user.click(screen.getByRole('combobox'));
    await user.click(screen.getByRole('option', { name: 'prod' }));

    expect(handle).toHaveBeenCalledWith('prod');
  });
});
