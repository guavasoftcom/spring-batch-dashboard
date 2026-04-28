import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import EnvironmentSelector from '~/components/EnvironmentSelector/EnvironmentSelector';

describe('EnvironmentSelector', () => {
  it('renders the selected option as the value', () => {
    render(
      <EnvironmentSelector value="prod" options={['dev', 'prod']} onChange={() => {}} />,
    );

    expect(screen.getByRole('combobox')).toHaveTextContent('prod');
  });

  it('falls back to an empty value when the supplied value is not in options', () => {
    render(
      <EnvironmentSelector value="ghost" options={['dev', 'prod']} onChange={() => {}} />,
    );

    expect(screen.getByRole('combobox')).not.toHaveTextContent('ghost');
  });

  it('renders a skeleton instead of the select when loading', () => {
    render(
      <EnvironmentSelector value="prod" options={['prod']} onChange={() => {}} loading />,
    );

    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
  });

  it('disables the control when no options are available', () => {
    render(<EnvironmentSelector value="" options={[]} onChange={() => {}} />);

    expect(screen.getByRole('combobox')).toHaveAttribute('aria-disabled', 'true');
  });

  it('invokes onChange with the picked value', async () => {
    const user = userEvent.setup();
    const handle = vi.fn();
    render(
      <EnvironmentSelector value="dev" options={['dev', 'prod']} onChange={handle} />,
    );

    await user.click(screen.getByRole('combobox'));
    await user.click(screen.getByRole('option', { name: 'prod' }));

    expect(handle).toHaveBeenCalledWith('prod');
  });
});
