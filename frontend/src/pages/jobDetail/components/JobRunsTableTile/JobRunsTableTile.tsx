import {
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TableSortLabel,
} from '@mui/material';
import type { RunSortField, SortDir } from '~/api/jobRunsApi';
import { ExecutionLink, LargeTile } from '~/components';
import type { JobRun, RunStatus } from '~/types';

const statusColor: Record<RunStatus, 'success' | 'error' | 'info'> = {
  COMPLETED: 'success',
  FAILED: 'error',
  STARTED: 'info',
};

type Column = {
  field: RunSortField;
  label: string;
  align?: 'right';
  noWrap?: boolean;
};

const columns: Column[] = [
  { field: 'executionId', label: 'Execution ID' },
  { field: 'status', label: 'Status' },
  { field: 'startTime', label: 'Started' },
  { field: 'endTime', label: 'Completed' },
  { field: 'durationSeconds', label: 'Duration (s)', align: 'right', noWrap: true },
  { field: 'readCount', label: 'Read', align: 'right' },
  { field: 'writeCount', label: 'Write', align: 'right' },
  { field: 'exitCode', label: 'Exit Code' },
];

type Props = {
  data: JobRun[] | null;
  loading: boolean;
  error: string | null;
  sortBy: RunSortField;
  sortDir: SortDir;
  onSortChange: (field: RunSortField) => void;
  onRunClick: (executionId: number) => void;
  totalElements: number;
  page: number;
  pageSize: number;
  onPageChange: (page: number) => void;
};

const JobRunsTableTile = ({
  data,
  loading,
  error,
  sortBy,
  sortDir,
  onSortChange,
  onRunClick,
  totalElements,
  page,
  pageSize,
  onPageChange,
}: Props) => (
  <LargeTile title="Job Runs" loading={loading} error={error} loadingHeight={200}>
    {data && (
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell
                  key={col.field}
                  align={col.align}
                  sortDirection={sortBy === col.field ? sortDir : false}
                  sx={col.noWrap ? { whiteSpace: 'nowrap' } : undefined}
                >
                  <TableSortLabel
                    active={sortBy === col.field}
                    direction={sortBy === col.field ? sortDir : 'asc'}
                    onClick={() => onSortChange(col.field)}
                  >
                    {col.label}
                  </TableSortLabel>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((run) => (
              <TableRow key={run.executionId} hover>
                <TableCell>
                  <ExecutionLink executionId={run.executionId} onClick={onRunClick} />
                </TableCell>
                <TableCell>
                  <Chip label={run.status} color={statusColor[run.status]} size="small" />
                </TableCell>
                <TableCell>{run.startTime}</TableCell>
                <TableCell>{run.endTime ?? '—'}</TableCell>
                <TableCell align="right">{run.durationSeconds}</TableCell>
                <TableCell align="right">{run.readCount.toLocaleString()}</TableCell>
                <TableCell align="right">{run.writeCount.toLocaleString()}</TableCell>
                <TableCell>{run.exitCode}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={totalElements}
          page={page}
          rowsPerPage={pageSize}
          onPageChange={(_, next) => onPageChange(next)}
          rowsPerPageOptions={[pageSize]}
        />
      </TableContainer>
    )}
  </LargeTile>
);

export default JobRunsTableTile;
