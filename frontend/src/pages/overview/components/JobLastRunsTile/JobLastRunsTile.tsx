import {
  Chip,
  Link as MuiLink,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { ExecutionLink, LargeTile } from '~/components';
import type { JobLastRun, RunStatus } from '~/types';
import { humanize } from '~/utils';

const statusColor: Record<RunStatus, 'success' | 'error' | 'info'> = {
  COMPLETED: 'success',
  FAILED: 'error',
  STARTED: 'info',
};

type Props = {
  data: JobLastRun[] | null;
  loading: boolean;
  error: string | null;
  onJobClick: (jobName: string) => void;
  onRunClick: (jobName: string, executionId: number) => void;
};

const JobLastRunsTile = ({ data, loading, error, onJobClick, onRunClick }: Props) => (
  <LargeTile title="Last Run by Job" loading={loading} error={error} loadingHeight={200}>
    {data && (
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Job</TableCell>
              <TableCell>Execution</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Started</TableCell>
              <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>Duration (s)</TableCell>
              <TableCell align="right">Read</TableCell>
              <TableCell align="right">Write</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.length === 0 && (
              <TableRow>
                <TableCell colSpan={7}>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                    No jobs in this environment.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
            {data.map(({ jobName, run }) => (
              <TableRow key={jobName} hover>
                <TableCell>
                  <MuiLink
                    component="button"
                    onClick={() => onJobClick(jobName)}
                    sx={{
                      fontWeight: 700,
                      color: 'primary.dark',
                      textDecoration: 'none',
                      background: 'none',
                      border: 0,
                      p: 0,
                      cursor: 'pointer',
                      '&:hover': { textDecoration: 'underline' },
                    }}
                  >
                    {humanize(jobName)}
                  </MuiLink>
                </TableCell>
                {run ? (
                  <>
                    <TableCell>
                      <ExecutionLink
                        executionId={run.executionId}
                        onClick={(executionId) => onRunClick(jobName, executionId)}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip label={run.status} color={statusColor[run.status]} size="small" />
                    </TableCell>
                    <TableCell>{run.startTime}</TableCell>
                    <TableCell align="right">{run.durationSeconds}</TableCell>
                    <TableCell align="right">{run.readCount.toLocaleString()}</TableCell>
                    <TableCell align="right">{run.writeCount.toLocaleString()}</TableCell>
                  </>
                ) : (
                  <TableCell colSpan={6}>
                    <Typography variant="body2" sx={{ color: 'text.disabled' }}>
                      No runs in this window
                    </Typography>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    )}
  </LargeTile>
);

export default JobLastRunsTile;
