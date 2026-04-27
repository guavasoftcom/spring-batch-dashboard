import type { ReactNode } from 'react';
import { Skeleton, Typography } from '@mui/material';
import { appColors } from '~/theme';
import { TilePaper } from '~/components/TilePaper';

type Props = {
  title: string;
  value?: ReactNode;
  subtitle?: ReactNode;
  loading?: boolean;
  error?: string | null;
  empty?: ReactNode;
  loadingValueWidth?: number | string;
  loadingSubtitleWidth?: number | string;
};

const StatTile = ({
  title,
  value,
  subtitle,
  loading = false,
  error = null,
  empty,
  loadingValueWidth = 80,
  loadingSubtitleWidth = '80%',
}: Props) => (
  <TilePaper>
    <Typography variant="body2" sx={{ color: appColors.brandOrange, fontWeight: 700 }}>
      {title}
    </Typography>
    {loading && (
      <>
        <Skeleton variant="text" width={loadingValueWidth} height={48} sx={{ mt: 1 }} />
        <Skeleton variant="text" width={loadingSubtitleWidth} sx={{ mt: 1 }} />
      </>
    )}
    {error && <Typography color="error" variant="body2" sx={{ mt: 1 }}>{error}</Typography>}
    {!loading && !error && value !== undefined && value !== null && (
      <>
        <Typography variant="h4" sx={{ mt: 1, color: appColors.brandBlueDark, fontWeight: 800 }}>
          {value}
        </Typography>
        {subtitle && (
          <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
            {subtitle}
          </Typography>
        )}
      </>
    )}
    {!loading && !error && (value === undefined || value === null) && empty && (
      <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
        {empty}
      </Typography>
    )}
  </TilePaper>
);

export default StatTile;
