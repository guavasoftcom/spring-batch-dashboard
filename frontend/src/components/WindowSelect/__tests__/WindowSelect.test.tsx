import { fireEvent, render, screen, within } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import WindowSelect from '~/components/WindowSelect/WindowSelect';
import { WindowContext } from '~/shell/WindowContext';

const renderWithWindow = (windowDays: number, setWindowDays = vi.fn(), label?: string) =>
  render(
    <WindowContext.Provider value={{ windowDays, setWindowDays }}>
      <WindowSelect label={label} />
    </WindowContext.Provider>,
  );

describe('WindowSelect', () => {
  it('renders the configured label and the currently selected option from context', () => {
    renderWithWindow(30);

    expect(screen.getByLabelText('Window')).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toHaveTextContent('Last 30 days');
  });

  it('honours a custom label', () => {
    renderWithWindow(7, vi.fn(), 'Range');

    expect(screen.getByLabelText('Range')).toBeInTheDocument();
  });

  it('forwards the picked value as a number to setWindowDays', () => {
    const setWindowDays = vi.fn();
    renderWithWindow(7, setWindowDays);

    fireEvent.mouseDown(screen.getByRole('combobox'));
    fireEvent.click(within(screen.getByRole('listbox')).getByText('Last 90 days'));

    expect(setWindowDays).toHaveBeenCalledWith(90);
  });
});
