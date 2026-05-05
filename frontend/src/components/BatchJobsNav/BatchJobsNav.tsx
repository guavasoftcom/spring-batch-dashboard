import {
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Skeleton,
  Tooltip,
  Typography,
} from '@mui/material';
import SourceIcon from '@mui/icons-material/Source';
import { humanize } from '~/utils';

type Props = {
  jobs: string[];
  activeJobId: string | null;
  loading: boolean;
  collapsed?: boolean;
  onSelect: (jobId: string) => void;
};

const BatchJobsNav = ({ jobs, activeJobId, loading, collapsed = false, onSelect }: Props) => (
  <>
    {!collapsed && (
      <Typography
        variant="overline"
        sx={{ px: 2, color: 'text.secondary', fontWeight: 700, letterSpacing: 1 }}
      >
        Batch Jobs
      </Typography>
    )}
    <List>
      {loading && !collapsed &&
        [0, 1, 2].map((i) => (
          <Skeleton
            key={i}
            variant="text"
            width="80%"
            sx={{ mx: 2, my: 0.5 }}
          />
        ))}
      {!loading && jobs.length === 0 && !collapsed && (
        <Typography variant="body2" sx={{ px: 2, color: 'text.secondary' }}>
          No jobs found
        </Typography>
      )}
      {!loading &&
        jobs.map((jobId) => {
          const selected = jobId === activeJobId;
          const label = humanize(jobId);
          const button = (
            <ListItemButton
              key={jobId}
              selected={selected}
              onClick={() => onSelect(jobId)}
              aria-label={label}
              sx={{
                mx: 1,
                borderRadius: 1,
                justifyContent: collapsed ? 'center' : 'flex-start',
                minWidth: 0,
                '&.Mui-selected': {
                  bgcolor: 'rgba(21, 101, 192, 0.12)',
                  color: 'primary.dark',
                },
                '&.Mui-selected:hover': { bgcolor: 'rgba(21, 101, 192, 0.18)' },
              }}
            >
              <ListItemIcon sx={{ minWidth: collapsed ? 0 : 32, color: 'inherit', justifyContent: 'center' }}>
                <SourceIcon fontSize="small" />
              </ListItemIcon>
              {!collapsed && (
                <ListItemText
                  primary={label}
                  slotProps={{
                    primary: {
                      noWrap: true,
                      sx: { fontWeight: selected ? 700 : 500, fontSize: 14 },
                    },
                  }}
                  sx={{ minWidth: 0 }}
                />
              )}
            </ListItemButton>
          );

          return collapsed ? (
            <Tooltip key={jobId} title={label} placement="right">
              {button}
            </Tooltip>
          ) : (
            button
          );
        })}
    </List>
  </>
);

export default BatchJobsNav;
