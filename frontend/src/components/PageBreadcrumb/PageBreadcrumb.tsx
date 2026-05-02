import { Fragment, useMemo, type ReactNode } from 'react';
import { Box, Link as MuiLink, Typography, useTheme } from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useQuery } from '@tanstack/react-query';
import { getEnvironments } from '~/api';
import { DatabaseIcon } from '~/components/DatabaseIcon';
import { useEnvironment } from '~/shell/EnvironmentContext';
import { useNav } from '~/shell/NavContext';
import type { EnvironmentInfo } from '~/types';
import { humanize } from '~/utils';

export type PageBreadcrumbSegment = {
  label: string;
  onClick?: () => void;
  icon?: ReactNode;
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
  textDecoration: 'none',
  background: 'none',
  border: 0,
  p: 0,
  cursor: 'pointer',
  '&:hover': { textDecoration: 'underline' },
};

const PageBreadcrumb = ({ segments }: Props) => {
  const theme = useTheme();
  const { environment } = useEnvironment();
  const { navOpen } = useNav();
  const { data: environments } = useQuery<EnvironmentInfo[]>({
    queryKey: ['environments'],
    queryFn: getEnvironments,
  });

  const currentEnvType = environments?.find((env) => env.name === environment)?.type;

  const visibleSegments = useMemo<PageBreadcrumbSegment[]>(() => {
    if (!navOpen && environment) {
      return [
        {
          label: environment,
          icon: currentEnvType ? <DatabaseIcon type={currentEnvType} sx={{ fontSize: 28 }} /> : undefined,
        },
        ...segments,
      ];
    }
    return segments;
  }, [navOpen, environment, currentEnvType, segments]);

  return (
  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, flexWrap: 'wrap' }}>
    {visibleSegments.map((segment, idx) => {
      const color = idx === 0 ? theme.palette.secondary.main : theme.palette.primary.dark;
      return (
        <Fragment key={`${idx}-${segment.label}`}>
          {idx > 0 && (
            <ChevronRightIcon
              sx={{ color: 'text.secondary', mx: 0.5, fontSize: 28 }}
            />
          )}
          {segment.icon && (
            <Box sx={{ display: 'inline-flex', alignItems: 'center', mr: 0.75, color }}>
              {segment.icon}
            </Box>
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
};

export default PageBreadcrumb;
