import type { ReactNode } from 'react';
import { Box, Skeleton, Typography } from '@mui/material';
import { appColors } from '~/theme';
import { TilePaper } from '~/components/TilePaper';

type Props = {
  title: string;
  headerAction?: ReactNode;
  loading?: boolean;
  error?: string | null;
  loadingHeight?: number;
  loadingSkeleton?: ReactNode;
  minHeight?: number;
  children?: ReactNode;
};

const LargeTile = ({
  title,
  headerAction,
  loading = false,
  error = null,
  loadingHeight = 220,
  loadingSkeleton,
  minHeight,
  children,
}: Props) => (
  <TilePaper minHeight={minHeight}>
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
      <Typography variant="h6" sx={{ color: appColors.brandBlueDark, fontWeight: 700 }}>
        {title}
      </Typography>
      {headerAction}
    </Box>
    {loading && (loadingSkeleton ?? (
      <Skeleton variant="rectangular" height={loadingHeight} sx={{ borderRadius: 1 }} />
    ))}
    {error && !loading && <Typography color="error">{error}</Typography>}
    {!loading && !error && children}
  </TilePaper>
);

export default LargeTile;
