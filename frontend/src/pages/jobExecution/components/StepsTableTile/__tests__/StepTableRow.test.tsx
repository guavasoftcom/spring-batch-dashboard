import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Table, TableBody } from '@mui/material';
import { describe, expect, it, vi } from 'vitest';
import StepTableRow from '~/pages/jobExecution/components/StepsTableTile/StepTableRow';
import type { StepRow } from '~/pages/jobExecution/types';

const baseStep: StepRow = {
  id: 1,
  stepName: 'loadCustomers',
  status: 'COMPLETED',
  readCount: 1234,
  writeCount: 1230,
  skipCount: 4,
  rollbackCount: 0,
  durationSeconds: 12,
  startTime: '2026-01-01T00:00:00Z',
  endTime: '2026-01-01T00:00:12Z',
};

const wrap = (step: StepRow, onClick: (id: number) => void = () => {}) => (
  <Table>
    <TableBody>
      <StepTableRow step={step} onClick={onClick} />
    </TableBody>
  </Table>
);

describe('StepTableRow', () => {
  it('renders the step name and counts', () => {
    render(wrap(baseStep));

    expect(screen.getByText('Load Customers')).toBeInTheDocument();
    expect(screen.getByText('1,234')).toBeInTheDocument();
    expect(screen.getByText('1,230')).toBeInTheDocument();
  });

  it('falls back to em-dash when endTime is null', () => {
    render(wrap({ ...baseStep, endTime: null }));
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('fires onClick with the step id when the row is clicked', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(wrap(baseStep, onClick));

    await user.click(screen.getByText('Load Customers'));
    expect(onClick).toHaveBeenCalledWith(1);
  });
});
