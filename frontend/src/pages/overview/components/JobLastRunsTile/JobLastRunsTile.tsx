import {
  Box,
  Checkbox,
  Chip,
  Link as MuiLink,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import DisplaySettingsIcon from '@mui/icons-material/DisplaySettings';
import ShowChartIcon from '@mui/icons-material/ShowChart';
import { ExecutionLink, InProgressTimestamp, LargeTile } from '~/components';
import type { JobLastRun } from '~/types';
import { STATUS_COLOR, formatDuration, formatTimestamp, humanize } from '~/utils';
import { MAX_CHARTED_JOBS } from '../../ChartedJobsContext';

type Props = {
  data: JobLastRun[] | null;
  loading: boolean;
  error: string | null;
  chartableJobs: string[];
  chartedJobs: string[];
  colorByJob: Record<string, string>;
  onJobClick: (jobName: string) => void;
  onRunClick: (jobName: string, executionId: number) => void;
  onToggleChart: (jobName: string) => void;
};

type ChartToggleProps = {
  jobName: string;
  chartable: boolean;
  charted: boolean;
  limitReached: boolean;
  color: string | undefined;
  onToggle: (jobName: string) => void;
};

const chartToggleHint = ({ chartable, charted, limitReached }: Omit<ChartToggleProps, 'jobName' | 'color' | 'onToggle'>) => {
  if (!chartable) {
    return 'No completed runs in this window';
  }
  if (charted) {
    return 'Hide from Job Duration Trends';
  }
  return limitReached
    ? `Up to ${MAX_CHARTED_JOBS} jobs can be charted at once`
    : 'Show on Job Duration Trends';
};

const ChartToggle = ({ jobName, chartable, charted, limitReached, color, onToggle }: ChartToggleProps) => {
  const disabled = !chartable || (!charted && limitReached);
  return (
    <Tooltip title={chartToggleHint({ chartable, charted, limitReached })}>
      {/* span: disabled buttons don't fire the pointer events Tooltip listens for */}
      <span>
        <Checkbox
          size="small"
          checked={charted}
          disabled={disabled}
          onChange={() => onToggle(jobName)}
          slotProps={{ input: { 'aria-label': `Chart ${humanize(jobName)}` } }}
          sx={{ '&.Mui-checked': { color } }}
        />
      </span>
    </Tooltip>
  );
};

const JobLastRunsTile = ({
  data,
  loading,
  error,
  chartableJobs,
  chartedJobs,
  colorByJob,
  onJobClick,
  onRunClick,
  onToggleChart,
}: Props) => (
  <LargeTile title="Last Run by Job" loading={loading} error={error} loadingHeight={200}>
    {data && (
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell padding="checkbox" align="center">
                <Tooltip title={`Charted on Job Duration Trends (max ${MAX_CHARTED_JOBS})`}>
                  <ShowChartIcon fontSize="small" sx={{ verticalAlign: 'middle', color: 'text.secondary' }} />
                </Tooltip>
              </TableCell>
              <TableCell>Job</TableCell>
              <TableCell>Execution</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Started</TableCell>
              <TableCell>Completed</TableCell>
              <TableCell align="right">Duration</TableCell>
              <TableCell align="right">Read</TableCell>
              <TableCell align="right">Write</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.length === 0 && (
              <TableRow>
                <TableCell colSpan={9}>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                    No jobs in this environment.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
            {data.map(({ jobName, run }) => (
              <TableRow key={jobName} hover>
                <TableCell padding="checkbox" align="center">
                  <ChartToggle
                    jobName={jobName}
                    chartable={chartableJobs.includes(jobName)}
                    charted={chartedJobs.includes(jobName)}
                    limitReached={chartedJobs.length >= MAX_CHARTED_JOBS}
                    color={colorByJob[jobName]}
                    onToggle={onToggleChart}
                  />
                </TableCell>
                <TableCell>
                  <MuiLink
                    component="button"
                    onClick={() => onJobClick(jobName)}
                    sx={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 0.75,
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
                    <DisplaySettingsIcon fontSize="small" sx={{ color: 'inherit' }} />
                    <Box component="span">{humanize(jobName)}</Box>
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
                      <Chip label={run.status} color={STATUS_COLOR[run.status]} size="small" />
                    </TableCell>
                    <TableCell>{formatTimestamp(run.startTime)}</TableCell>
                    <TableCell><InProgressTimestamp value={run.endTime} /></TableCell>
                    <TableCell align="right">{formatDuration(run.durationSeconds)}</TableCell>
                    <TableCell align="right">{run.readCount.toLocaleString()}</TableCell>
                    <TableCell align="right">{run.writeCount.toLocaleString()}</TableCell>
                  </>
                ) : (
                  <TableCell colSpan={7}>
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
