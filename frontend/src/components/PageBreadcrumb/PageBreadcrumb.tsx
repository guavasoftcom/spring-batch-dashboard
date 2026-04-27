import { Fragment } from 'react';
import { Box, Link as MuiLink, Typography } from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { appColors } from '~/theme';
import { humanize } from '~/utils';

export type PageBreadcrumbSegment = {
  label: string;
  onClick?: () => void;
};

type Props = {
  segments: PageBreadcrumbSegment[];
};

const segmentTextSx = {
  fontWeight: 800,
  fontSize: '1.5rem',
  lineHeight: 1.334,
};

const linkSx = {
  ...segmentTextSx,
  color: appColors.brandBlueDark,
  textDecoration: 'none',
  background: 'none',
  border: 0,
  p: 0,
  cursor: 'pointer',
  '&:hover': { textDecoration: 'underline' },
};

const PageBreadcrumb = ({ segments }: Props) => (
  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, flexWrap: 'wrap' }}>
    {segments.map((segment, idx) => {
      const color = idx === 0 ? appColors.brandOrange : appColors.brandBlueDark;
      return (
        <Fragment key={`${idx}-${segment.label}`}>
          {idx > 0 && (
            <ChevronRightIcon
              sx={{ color: 'text.secondary', mx: 0.5, fontSize: 28 }}
            />
          )}
          {segment.onClick ? (
            <MuiLink
              component="button"
              onClick={segment.onClick}
              sx={{ ...linkSx, color }}
            >
              {humanize(segment.label)}
            </MuiLink>
          ) : (
            <Typography variant="h5" sx={{ color, fontWeight: 800 }}>
              {humanize(segment.label)}
            </Typography>
          )}
        </Fragment>
      );
    })}
  </Box>
);

export default PageBreadcrumb;
