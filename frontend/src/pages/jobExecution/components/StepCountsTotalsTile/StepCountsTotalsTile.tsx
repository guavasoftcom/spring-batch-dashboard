import { Box } from '@mui/material';
import { LargeTile, StepCountsBarChart } from '~/components';
import type { StepCountsSummary } from '~/types';

type Props = {
  data: StepCountsSummary | null;
  loading: boolean;
  error: string | null;
};

const StepCountsTotalsTile = ({ data, loading, error }: Props) => (
  <LargeTile
    title="Counts Across All Steps"
    loading={loading}
    error={error}
    minHeight={340}
    // Reserve the chart's exact height with an invisible spacer instead of a
    // shimmery skeleton; keeps the container geometry stable across the
    // loading→data swap so MUI x-charts can paint its enter animation.
    loadingSkeleton={<Box sx={{ height: 300 }} />}
  >
    {data && <StepCountsBarChart data={data} />}
  </LargeTile>
);

export default StepCountsTotalsTile;
