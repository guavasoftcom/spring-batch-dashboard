import type { ReactNode } from 'react';
import { Paper } from '@mui/material';
import { appColors } from '~/theme';

type Props = { children: ReactNode; minHeight?: number };

const TilePaper = ({ children, minHeight }: Props) => (
  <Paper
    elevation={0}
    sx={{
      p: 2.25,
      borderRadius: 2,
      border: '1px solid #D5DBE3',
      backgroundColor: appColors.white,
      boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
      height: '100%',
      minHeight,
    }}
  >
    {children}
  </Paper>
);

export default TilePaper;
