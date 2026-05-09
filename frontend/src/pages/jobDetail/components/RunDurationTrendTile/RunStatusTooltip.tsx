import { Box, Chip, Paper, Typography } from '@mui/material';
import { ChartsTooltipContainer, useAxesTooltip } from '@mui/x-charts';
import type { RunStatus } from '~/types';
import { STATUS_COLOR } from '~/utils';

/**
 * Custom tooltip slot that draws each run's status as a colored Chip in the header, then lists
 * the visible series with their value. Replaces the default axis-mode tooltip so the status
 * badge matches the JobRunsTableTile palette.
 */
export const createRunStatusTooltip = (
  runs: { executionId: number; status: RunStatus }[],
) => {
  // Always render `ChartsTooltipContainer` so the Popper anchor stays mounted across
  // hover gaps. Returning null when there's no hover data unmounts the container, and
  // the next hover briefly renders at Popper's default position (0,0) before reattaching
  // to the pointer — that's the top-left flash. Gating the inner content (instead of the
  // whole container) keeps the anchor stable while still hiding the body when idle.
  const RunStatusTooltip = () => {
    const tooltipData = useAxesTooltip();
    const axis = tooltipData?.[0];
    const run = axis ? runs[axis.dataIndex] : null;
    return (
      <ChartsTooltipContainer trigger="axis">
        {run && axis && (
          <Paper
            elevation={3}
            sx={(theme) => ({
              px: 1.5,
              py: 1,
              border: `1px solid ${theme.palette.divider}`,
              backgroundColor: theme.palette.background.paper,
              color: theme.palette.text.primary,
              minWidth: 160,
            })}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.75 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                {`#${run.executionId}`}
              </Typography>
              <Chip label={run.status} size="small" color={STATUS_COLOR[run.status]} />
            </Box>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.25 }}>
              {axis.seriesItems.map((item) => (
                <Box key={String(item.seriesId)} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Box sx={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: item.color }} />
                  <Typography variant="body2" sx={{ flex: 1 }}>{item.formattedLabel ?? String(item.seriesId)}</Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>{item.formattedValue}</Typography>
                </Box>
              ))}
            </Box>
          </Paper>
        )}
      </ChartsTooltipContainer>
    );
  };
  return RunStatusTooltip;
};
