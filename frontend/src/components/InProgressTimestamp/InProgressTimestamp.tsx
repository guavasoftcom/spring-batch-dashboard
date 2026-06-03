import { Typography } from '@mui/material';
import { formatTimestamp } from '~/utils';

type Props = { value: string | null };

const InProgressTimestamp = ({ value }: Props) =>
  value ? (
    <>{formatTimestamp(value)}</>
  ) : (
    <Typography
      component="span"
      variant="body2"
      sx={{ color: 'text.disabled', fontStyle: 'italic' }}
    >
      In progress
    </Typography>
  );

export default InProgressTimestamp;
