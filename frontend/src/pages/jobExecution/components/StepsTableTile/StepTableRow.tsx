import { Chip, TableCell, TableRow } from '@mui/material';
import { InProgressTimestamp } from '~/components';
import type { StepRow } from '~/pages/jobExecution/types';
import { STATUS_COLOR, formatDuration, formatTimestamp, humanize } from '~/utils';

type Props = {
  step: StepRow;
  onClick: (stepExecutionId: number) => void;
};

const StepTableRow = ({ step, onClick }: Props) => (
  <TableRow hover onClick={() => onClick(step.id)} sx={{ cursor: 'pointer' }}>
    <TableCell>{humanize(step.stepName)}</TableCell>
    <TableCell>
      <Chip label={step.status} color={STATUS_COLOR[step.status]} size="small" />
    </TableCell>
    <TableCell align="right">{step.readCount.toLocaleString()}</TableCell>
    <TableCell align="right">{step.writeCount.toLocaleString()}</TableCell>
    <TableCell align="right">{step.skipCount}</TableCell>
    <TableCell align="right">{step.rollbackCount}</TableCell>
    <TableCell align="right">{formatDuration(step.durationSeconds)}</TableCell>
    <TableCell>{formatTimestamp(step.startTime)}</TableCell>
    <TableCell><InProgressTimestamp value={step.endTime} /></TableCell>
  </TableRow>
);

export default StepTableRow;
