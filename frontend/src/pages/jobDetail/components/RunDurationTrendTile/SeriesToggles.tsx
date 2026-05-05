import { Box, Chip } from '@mui/material';
import { appColors } from '~/theme';

export type SeriesId = 'duration' | 'read' | 'write';

export const SERIES: ReadonlyArray<{ id: SeriesId; label: string; color: string }> = [
  { id: 'duration', label: 'Duration', color: appColors.brandBlueLight },
  { id: 'read',     label: 'Read',     color: '#66BB6A' },
  { id: 'write',    label: 'Write',    color: '#FFA726' },
];

type Props = {
  hidden: Set<SeriesId>;
  onToggle: (id: SeriesId) => void;
};

const SeriesToggles = ({ hidden, onToggle }: Props) => (
  <Box sx={{ display: 'flex', gap: 0.75 }}>
    {SERIES.map(({ id, label, color }) => {
      const isHidden = hidden.has(id);
      return (
        <Chip
          key={id}
          size="small"
          label={label}
          onClick={() => onToggle(id)}
          sx={{
            fontWeight: 600,
            cursor: 'pointer',
            backgroundColor: isHidden ? 'transparent' : color,
            color: isHidden ? color : '#FFFFFF',
            border: `1px solid ${color}`,
          }}
        />
      );
    })}
  </Box>
);

export default SeriesToggles;
