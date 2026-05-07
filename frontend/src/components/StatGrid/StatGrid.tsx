import { Box, Typography } from '@mui/material';

export type StatGridEntry = {
  label: string;
  value: React.ReactNode;
  /** When > 1, the cell spans this many columns of the grid. */
  colSpan?: number;
};

type StatCellProps = { label: string; value: React.ReactNode; colSpan?: number };

const StatCell = ({ label, value, colSpan }: StatCellProps) => (
  <Box sx={{ gridColumn: colSpan && colSpan > 1 ? `span ${colSpan}` : undefined, minWidth: 0 }}>
    <Typography
      component="div"
      sx={{ color: 'text.secondary', fontWeight: 700, fontSize: 11, mb: 0.25 }}
    >
      {label}
    </Typography>
    <Typography
      component="div"
      sx={{
        color: 'text.primary',
        fontFamily: 'monospace',
        fontSize: 13,
        wordBreak: 'break-word',
        whiteSpace: 'pre-wrap',
      }}
    >
      {value ?? '—'}
    </Typography>
  </Box>
);

type Props = {
  entries: StatGridEntry[];
  /** Number of equal-width columns. Defaults to 4. */
  columns?: number;
};

/**
 * Compact "label above value" grid rendered in an inset card. Cells flow into a
 * `columns`-wide CSS grid; individual cells can opt to span more columns via
 * `entry.colSpan`. Designed for read-only detail panes (e.g. inside a modal).
 */
const StatGrid = ({ entries, columns = 4 }: Props) => (
  <Box
    sx={(theme) => ({
      display: 'grid',
      gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))`,
      columnGap: 2,
      rowGap: 1.5,
      bgcolor: theme.palette.surface.inset,
      border: 1,
      borderColor: 'divider',
      borderRadius: 1,
      p: 1.5,
    })}
  >
    {entries.map((entry) => (
      <StatCell key={entry.label} label={entry.label} value={entry.value} colSpan={entry.colSpan} />
    ))}
  </Box>
);

const formatPrimitive = (value: unknown): string => {
  if (value === null || value === undefined) {
    return '—';
  }
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }
  return JSON.stringify(value);
};

/**
 * Adapts a `Record<string, unknown>` (e.g. a parsed Spring Batch execution context)
 * into `StatGridEntry[]` ready to feed `<StatGrid>`. Primitive values render as-is;
 * objects/arrays are JSON-stringified.
 */
export const recordToStatEntries = (record: Record<string, unknown>): StatGridEntry[] =>
  Object.entries(record).map(([key, value]) => ({ label: key, value: formatPrimitive(value) }));

export default StatGrid;
