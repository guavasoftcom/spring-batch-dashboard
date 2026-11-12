import { Fragment } from 'react';
import {
  Box,
  Chip,
  Collapse,
  IconButton,
  InputAdornment,
  Link as MuiLink,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import DisplaySettingsIcon from '@mui/icons-material/DisplaySettings';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import SearchIcon from '@mui/icons-material/Search';
import { ExecutionLink, InProgressTimestamp, LargeTile } from '~/components';
import { STATUS_COLOR, formatDuration, formatTimestamp, humanize } from '~/utils';
import { isSlower, type JobFilter, type JobRow, type JobSort } from '../../jobInsights';
import JobDurationChart from './JobDurationChart';
import Sparkline from './Sparkline';

type Props = {
  rows: JobRow[] | null;
  visibleRows: JobRow[];
  counts: Record<JobFilter, number>;
  loading: boolean;
  error: string | null;
  windowDays: number;
  filter: JobFilter;
  query: string;
  sort: JobSort;
  expanded: ReadonlySet<string>;
  onFilterChange: (filter: JobFilter) => void;
  onQueryChange: (query: string) => void;
  onSortChange: (sort: JobSort) => void;
  onToggleExpanded: (jobName: string) => void;
  onJobClick: (jobName: string) => void;
  onRunClick: (jobName: string, executionId: number) => void;
};

const FILTER_LABELS: Record<JobFilter, string> = {
  all: 'All',
  failed: 'Failed',
  running: 'Running',
  slower: 'Slower than usual',
  completed: 'Completed',
};

const SORT_LABELS: Record<JobSort, string> = {
  attention: 'Needs attention first',
  slowdown: 'Biggest slowdown',
  duration: 'Longest duration',
  name: 'Name',
};

const COLUMN_COUNT = 8;

const ChangeVsUsual = ({ row }: { row: JobRow }) => {
  if (row.changeVsUsual === null || row.usualSeconds === null || !row.run) {
    return <Typography component="span" variant="body2" sx={{ color: 'text.disabled' }}>—</Typography>;
  }
  const percent = Math.round(row.changeVsUsual * 100);
  const title = `Last run ${formatDuration(row.run.durationSeconds)} vs usual ${formatDuration(row.usualSeconds)}`;
  const slower = isSlower(row);
  const faster = row.changeVsUsual < -0.25;
  const text = slower ? `▲ ${percent}%` : faster ? `▼ ${Math.abs(percent)}%` : `${percent > 0 ? '+' : ''}${percent}%`;
  return (
    <Box
      component="span"
      title={title}
      sx={{
        px: 0.75,
        py: 0.125,
        borderRadius: 1,
        fontWeight: 600,
        fontSize: 13,
        color: slower ? 'warning.dark' : faster ? 'info.dark' : 'text.secondary',
        bgcolor: slower ? 'warning.light' : faster ? 'action.hover' : 'transparent',
      }}
    >
      {text}
    </Box>
  );
};

const JobLastRunsTile = ({
  rows,
  visibleRows,
  counts,
  loading,
  error,
  windowDays,
  filter,
  query,
  sort,
  expanded,
  onFilterChange,
  onQueryChange,
  onSortChange,
  onToggleExpanded,
  onJobClick,
  onRunClick,
}: Props) => (
  <LargeTile
    title="Jobs"
    headerAction={
      rows && rows.length > 0 && (
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {rows.length} jobs in the last {windowDays} days · expand a row for its duration chart
        </Typography>
      )
    }
    loading={loading}
    error={error}
    loadingHeight={200}
  >
    {rows && (
      <>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, justifyContent: 'space-between', mb: 1.5 }}>
          <Box role="group" aria-label="Filter by last run" sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
            {(Object.keys(FILTER_LABELS) as JobFilter[]).map((key) => (
              <Chip
                key={key}
                label={`${FILTER_LABELS[key]} ${counts[key]}`}
                size="small"
                variant={filter === key ? 'filled' : 'outlined'}
                color={filter === key ? 'primary' : 'default'}
                disabled={key !== 'all' && counts[key] === 0}
                aria-pressed={filter === key}
                onClick={() => onFilterChange(key)}
              />
            ))}
          </Box>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            <TextField
              size="small"
              type="search"
              placeholder="Search jobs"
              value={query}
              onChange={(event) => onQueryChange(event.target.value)}
              slotProps={{
                htmlInput: { 'aria-label': 'Search jobs' },
                input: {
                  startAdornment: (
                    <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>
                  ),
                },
              }}
              sx={{ width: 200 }}
            />
            <TextField
              select
              size="small"
              value={sort}
              onChange={(event) => onSortChange(event.target.value as JobSort)}
              slotProps={{ htmlInput: { 'aria-label': 'Sort jobs' } }}
            >
              {(Object.keys(SORT_LABELS) as JobSort[]).map((key) => (
                <MenuItem key={key} value={key}>{SORT_LABELS[key]}</MenuItem>
              ))}
            </TextField>
          </Box>
        </Box>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox" />
                <TableCell>Job</TableCell>
                <TableCell>Last run</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Duration</TableCell>
                <TableCell align="right">vs usual</TableCell>
                <TableCell>Trend ({windowDays}d)</TableCell>
                <TableCell align="right">Read / Write</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {visibleRows.length === 0 && (
                <TableRow>
                  <TableCell colSpan={COLUMN_COUNT}>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                      {rows.length === 0 ? 'No jobs in this environment.' : 'No jobs match these filters.'}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
              {visibleRows.map((row) => {
                const { jobName, run } = row;
                const isOpen = expanded.has(jobName);
                const label = humanize(jobName);
                return (
                  <Fragment key={jobName}>
                    <TableRow hover selected={isOpen} sx={{ '& > td': { borderBottom: isOpen ? 0 : undefined } }}>
                      <TableCell padding="checkbox">
                        <IconButton
                          size="small"
                          aria-expanded={isOpen}
                          aria-label={`Show duration chart for ${label}`}
                          disabled={row.points.length === 0}
                          onClick={() => onToggleExpanded(jobName)}
                        >
                          <KeyboardArrowRightIcon
                            fontSize="small"
                            sx={{ transform: isOpen ? 'rotate(90deg)' : 'none', transition: 'transform .15s' }}
                          />
                        </IconButton>
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
                          <Box component="span">{label}</Box>
                        </MuiLink>
                      </TableCell>
                      {run ? (
                        <>
                          <TableCell>
                            <Box sx={{ display: 'flex', flexDirection: 'column', lineHeight: 1.25 }}>
                              <span>{formatTimestamp(run.startTime)}</span>
                              <ExecutionLink
                                executionId={run.executionId}
                                onClick={(executionId) => onRunClick(jobName, executionId)}
                              />
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Chip label={run.status} color={STATUS_COLOR[run.status]} size="small" />
                          </TableCell>
                          <TableCell align="right">
                            {run.status === 'STARTED' ? (
                              <InProgressTimestamp value={run.endTime} />
                            ) : (
                              formatDuration(run.durationSeconds)
                            )}
                          </TableCell>
                          <TableCell align="right"><ChangeVsUsual row={row} /></TableCell>
                          <TableCell>
                            <Sparkline
                              points={row.points}
                              warn={isSlower(row)}
                              label={`${label} daily average duration, last ${windowDays} days`}
                            />
                          </TableCell>
                          <TableCell align="right">
                            {run.readCount.toLocaleString()} / {run.writeCount.toLocaleString()}
                          </TableCell>
                        </>
                      ) : (
                        <TableCell colSpan={COLUMN_COUNT - 2}>
                          <Typography variant="body2" sx={{ color: 'text.disabled' }}>
                            No runs in this window
                          </Typography>
                        </TableCell>
                      )}
                    </TableRow>
                    {row.points.length > 0 && (
                      <TableRow>
                        <TableCell colSpan={COLUMN_COUNT} sx={{ py: 0, borderBottom: isOpen ? undefined : 0 }}>
                          <Collapse in={isOpen} timeout="auto" unmountOnExit>
                            <Box sx={{ py: 1.5 }}>
                              <JobDurationChart row={row} />
                            </Box>
                          </Collapse>
                        </TableCell>
                      </TableRow>
                    )}
                  </Fragment>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      </>
    )}
  </LargeTile>
);

export default JobLastRunsTile;
