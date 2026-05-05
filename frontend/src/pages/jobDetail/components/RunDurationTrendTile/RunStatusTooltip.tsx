import { Box, Chip, Paper, Typography } from '@mui/material';
import { ChartsTooltipContainer, useAxesTooltip } from '@mui/x-charts';
import type { JobRun } from '~/types';

// Same status palette the JobRunsTableTile / StepTableRow use; keeps the visual vocabulary consistent.
const STATUS_COLOR: Record<JobRun['status'], 'success' | 'error' | 'info'> = {
  COMPLETED: 'success',
  FAILED: 'error',
  STARTED: 'info',
};

/**
 * Custom tooltip slot that draws each run's status as a colored Chip in the header, then lists
 * the visible series with their value. Replaces the default axis-mode tooltip so the status
 * badge matches the JobRunsTableTile palette.
 */
export const createRunStatusTooltip = (data: JobRun[]) => {
  const RunStatusTooltip = () => {
    const tooltipData = useAxesTooltip();
    if (!tooltipData?.length) {
      return null;
    }
    const axis = tooltipData[0];
    const run = data[axis.dataIndex];
    if (!run) {
      return null;
    }
    return (
      <ChartsTooltipContainer trigger="axis">
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
      </ChartsTooltipContainer>
    );
  };
  return RunStatusTooltip;
};
