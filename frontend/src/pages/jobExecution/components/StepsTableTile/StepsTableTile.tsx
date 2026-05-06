import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TableSortLabel,
} from '@mui/material';
import type { StepSortDir, StepSortField } from '~/api/jobExecutionStepsApi';
import { LargeTile } from '~/components';
import type { StepRow } from '~/pages/jobExecution/types';
import StepTableRow from './StepTableRow';

type Column = {
  field: StepSortField;
  label: string;
  align?: 'right';
  noWrap?: boolean;
};

const columns: Column[] = [
  { field: 'stepName', label: 'Step' },
  { field: 'status', label: 'Status' },
  { field: 'readCount', label: 'Read', align: 'right' },
  { field: 'writeCount', label: 'Write', align: 'right' },
  { field: 'skipCount', label: 'Skips', align: 'right' },
  { field: 'rollbackCount', label: 'Rollbacks', align: 'right' },
  { field: 'durationSeconds', label: 'Duration (s)', align: 'right', noWrap: true },
  { field: 'startTime', label: 'Started' },
  { field: 'endTime', label: 'Completed' },
];

type Props = {
  data: StepRow[] | null;
  loading: boolean;
  error: string | null;
  sortBy: StepSortField;
  sortDir: StepSortDir;
  onSortChange: (field: StepSortField) => void;
  totalElements: number;
  page: number;
  pageSize: number;
  onPageChange: (page: number) => void;
};

const StepsTableTile = ({
  data,
  loading,
  error,
  sortBy,
  sortDir,
  onSortChange,
  totalElements,
  page,
  pageSize,
  onPageChange,
}: Props) => (
  <LargeTile title="Steps" loading={loading} error={error}>
    {data && (
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell />
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
            {data.map((step) => (
              <StepTableRow key={step.id} step={step} />
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

export default StepsTableTile;
