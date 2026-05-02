import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Table, TableBody } from '@mui/material';
import { describe, expect, it } from 'vitest';
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
  exitCode: 'COMPLETED',
  exitMessage: null,
  context: { batchSize: 500 },
};

const wrap = (step: StepRow) => (
  <Table>
    <TableBody>
      <StepTableRow step={step} />
    </TableBody>
  </Table>
);

describe('StepTableRow', () => {
  it('renders the step name and counts collapsed by default', () => {
    render(wrap(baseStep));

    expect(screen.getByText('Load Customers')).toBeInTheDocument();
    expect(screen.getByText('1,234')).toBeInTheDocument();
    expect(screen.getByText('1,230')).toBeInTheDocument();
    expect(screen.queryByText('Exit Status')).not.toBeInTheDocument();
  });

  it('falls back to em-dash when endTime is null', () => {
    render(wrap({ ...baseStep, endTime: null }));
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('expands the detail section when the toggle is clicked', async () => {
    const user = userEvent.setup();
    render(wrap(baseStep));

    await user.click(screen.getByRole('button', { name: 'expand row' }));

    expect(screen.getByText('Exit Status')).toBeVisible();
    expect(screen.getByText('Execution Context')).toBeVisible();
    expect(screen.getByText(/batchSize/)).toBeVisible();
  });

  it('shows the failure reason only when exitMessage is set', async () => {
    const user = userEvent.setup();
    render(wrap({ ...baseStep, exitMessage: 'database connection refused' }));

    await user.click(screen.getByRole('button', { name: 'expand row' }));

    expect(screen.getByText('Failure Reason')).toBeVisible();
    expect(screen.getByText('database connection refused')).toBeVisible();
  });

  it('omits the failure reason block when exitMessage is null', async () => {
    const user = userEvent.setup();
    render(wrap(baseStep));

    await user.click(screen.getByRole('button', { name: 'expand row' }));

    expect(screen.queryByText('Failure Reason')).not.toBeInTheDocument();
  });
});
