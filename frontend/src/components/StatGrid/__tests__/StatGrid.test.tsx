import { ThemeProvider } from '@mui/material/styles';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { StatGrid, recordToStatEntries } from '~/components';
import { createAppTheme } from '~/theme';

const theme = createAppTheme('light');
const wrap = (ui: React.ReactElement) => render(<ThemeProvider theme={theme}>{ui}</ThemeProvider>);

describe('StatGrid', () => {
  it('renders each entry as a label/value pair', () => {
    wrap(
      <StatGrid
        entries={[
          { label: 'Started', value: '2026-04-30 09:15:30' },
          { label: 'Ended', value: '2026-04-30 09:16:00' },
        ]}
      />,
    );

    expect(screen.getByText('Started')).toBeInTheDocument();
    expect(screen.getByText('2026-04-30 09:15:30')).toBeInTheDocument();
    expect(screen.getByText('Ended')).toBeInTheDocument();
    expect(screen.getByText('2026-04-30 09:16:00')).toBeInTheDocument();
  });

  it('renders an em-dash for null/undefined values', () => {
    wrap(<StatGrid entries={[{ label: 'Exit code', value: null }]} />);
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('applies gridColumn span when colSpan > 1', () => {
    wrap(
      <StatGrid
        entries={[
          { label: 'Code', value: 'OK' },
          { label: 'Message', value: 'long', colSpan: 3 },
        ]}
      />,
    );

    // The cell with colSpan 3 should set its `gridColumn` style; the default cell does not.
    const messageCell = screen.getByText('Message').parentElement!;
    const codeCell = screen.getByText('Code').parentElement!;
    expect(messageCell).toHaveStyle({ gridColumn: 'span 3' });
    expect(codeCell).not.toHaveStyle({ gridColumn: 'span 3' });
  });

  it('honours the columns prop when laying out the CSS grid', () => {
    const { container } = wrap(<StatGrid columns={2} entries={[{ label: 'A', value: '1' }]} />);
    const grid = container.firstChild as HTMLElement;
    expect(grid).toHaveStyle({ gridTemplateColumns: 'repeat(2, minmax(0, 1fr))' });
  });
});

describe('recordToStatEntries', () => {
  it('passes string/number/boolean primitives through as-is', () => {
    const entries = recordToStatEntries({ source: 'orders.csv', batchSize: 500, dryRun: true });
    expect(entries).toEqual([
      { label: 'source', value: 'orders.csv' },
      { label: 'batchSize', value: '500' },
      { label: 'dryRun', value: 'true' },
    ]);
  });

  it('replaces null/undefined values with an em-dash', () => {
    expect(recordToStatEntries({ note: null })).toEqual([{ label: 'note', value: '—' }]);
  });

  it('JSON-stringifies object values', () => {
    expect(recordToStatEntries({ flags: { retry: true, attempts: 3 } })).toEqual([
      { label: 'flags', value: '{"retry":true,"attempts":3}' },
    ]);
  });
});
