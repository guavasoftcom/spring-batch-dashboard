import { useState } from 'react';
import {
  Box,
  Chip,
  Collapse,
  IconButton,
  TableCell,
  TableRow,
  Typography,
} from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import type { StepRow, StepStatus } from '~/pages/jobExecution/types';
import { humanize } from '~/utils';

const statusColor: Record<StepStatus, 'success' | 'error' | 'info'> = {
  COMPLETED: 'success',
  FAILED: 'error',
  STARTED: 'info',
};

const StepTableRow = ({ step }: { step: StepRow }) => {
  const [open, setOpen] = useState(false);
  return (
    <>
      <TableRow
        hover
        sx={open ? { '& > *': { borderBottom: 'unset' } } : undefined}
      >
        <TableCell sx={{ width: 48 }}>
          <IconButton
            size="small"
            onClick={() => setOpen((o) => !o)}
            aria-label="expand row"
          >
            {open ? (
              <KeyboardArrowDownIcon fontSize="small" />
            ) : (
              <KeyboardArrowRightIcon fontSize="small" />
            )}
          </IconButton>
        </TableCell>
        <TableCell>{humanize(step.stepName)}</TableCell>
        <TableCell>
          <Chip
            label={step.status}
            color={statusColor[step.status]}
            size="small"
          />
        </TableCell>
        <TableCell align="right">{step.readCount.toLocaleString()}</TableCell>
        <TableCell align="right">{step.writeCount.toLocaleString()}</TableCell>
        <TableCell align="right">{step.skipCount}</TableCell>
        <TableCell align="right">{step.rollbackCount}</TableCell>
        <TableCell align="right">{step.durationSeconds}</TableCell>
        <TableCell>{step.startTime}</TableCell>
        <TableCell>{step.endTime ?? '—'}</TableCell>
      </TableRow>
      <TableRow>
        <TableCell
          colSpan={10}
          sx={{ py: 0, borderBottom: open ? undefined : 'none' }}
        >
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box
              sx={{
                py: 2,
                px: 1,
                display: 'flex',
                flexDirection: 'column',
                gap: 1.5,
              }}
            >
              <Box>
                <Typography
                  variant="subtitle2"
                  sx={{ color: 'primary.dark', fontWeight: 700 }}
                >
                  Exit Status
                </Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                  {step.exitCode}
                </Typography>
              </Box>
              {step.exitMessage && (
                <Box>
                  <Typography
                    variant="subtitle2"
                    sx={{ color: 'primary.dark', fontWeight: 700 }}
                  >
                    Failure Reason
                  </Typography>
                  <Typography
                    variant="body2"
                    component="pre"
                    sx={{
                      color: 'error.main',
                      whiteSpace: 'pre-wrap',
                      fontFamily: 'monospace',
                      fontSize: 12,
                      m: 0,
                    }}
                  >
                    {step.exitMessage}
                  </Typography>
                </Box>
              )}
              <Box>
                <Typography
                  variant="subtitle2"
                  sx={{ color: 'primary.dark', fontWeight: 700 }}
                >
                  Execution Context
                </Typography>
                <Box
                  component="pre"
                  sx={{
                    bgcolor: 'background.default',
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    p: 1.5,
                    m: 0,
                    fontFamily: 'monospace',
                    fontSize: 12,
                    color: 'text.primary',
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    overflowWrap: 'anywhere',
                  }}
                >
                  {JSON.stringify(step.context, null, 2)}
                </Box>
              </Box>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
};

export default StepTableRow;
