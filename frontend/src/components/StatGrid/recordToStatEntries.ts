import type { StatGridEntry } from './StatGrid';

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
