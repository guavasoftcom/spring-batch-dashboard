/**
 * Maps a Spring Batch run/step status to a MUI semantic color slot used by `<Chip color="..." />`.
 * Centralized here so every list/tile/modal renders the same color for each status.
 */
export const STATUS_COLOR: Record<'COMPLETED' | 'FAILED' | 'STARTED', 'success' | 'error' | 'info'> = {
  COMPLETED: 'success',
  FAILED: 'error',
  STARTED: 'info',
};
