import { Chip, DialogTitle, IconButton, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TimelapseIcon from '@mui/icons-material/Timelapse';
import type { StepExecutionDetail } from '~/pages/jobExecution/types';
import { STATUS_COLOR, formatDuration, humanize } from '~/utils';

type Props = {
  data: StepExecutionDetail | null;
  onClose: () => void;
};

const StepDetailModalHeader = ({ data, onClose }: Props) => (
  <DialogTitle
    sx={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: 1.5,
      px: 6,
    }}
  >
    <Typography component="span" variant="h6" sx={{ fontWeight: 700 }}>
      {data ? `${humanize(data.stepName)} — Step #${data.id}` : 'Step Detail'}
    </Typography>
    {data && <Chip label={data.status} color={STATUS_COLOR[data.status]} size="small" />}
    {data && (
      <Chip
        icon={<TimelapseIcon />}
        label={formatDuration(data.durationSeconds)}
        size="small"
        variant="outlined"
      />
    )}
    <IconButton
      aria-label="close"
      onClick={onClose}
      sx={{ position: 'absolute', right: 8, top: 8, color: 'text.secondary' }}
    >
      <CloseIcon />
    </IconButton>
  </DialogTitle>
);

export default StepDetailModalHeader;
