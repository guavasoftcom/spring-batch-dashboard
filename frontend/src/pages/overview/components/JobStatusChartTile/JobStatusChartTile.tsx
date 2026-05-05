import { BarChart } from '@mui/x-charts';
import { LargeTile } from '~/components';
import type { JobStatusSlice } from '~/types';

type Props = {
  data: JobStatusSlice[] | null;
  loading: boolean;
  error: string | null;
};

const JobStatusChartTile = ({ data, loading, error }: Props) => (
  <LargeTile title="Job Status Distribution" loading={loading} error={error} minHeight={340}>
    {data && (
      <BarChart
        xAxis={[{
          scaleType: 'band',
          data: data.map((s) => s.label),
          tickLabelStyle: { fontSize: 12 },
          tickLabelInterval: () => true,
        }]}
        yAxis={[{ min: 0, label: 'Runs' }]}
        series={data.map((s) => ({
          data: data.map((d) => (d.id === s.id ? d.value : 0)),
          label: s.label,
          color: s.color,
          stack: 'total',
        }))}
        height={300}
        margin={20}
        slotProps={{ tooltip: { trigger: 'item' } }}
      />
    )}
  </LargeTile>
);

export default JobStatusChartTile;
