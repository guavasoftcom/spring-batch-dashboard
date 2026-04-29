import {
  List,
  ListItemButton,
  ListItemText,
  Skeleton,
  Typography,
} from '@mui/material';
import { humanize } from '~/utils';

type Props = {
  jobs: string[];
  activeJobId: string | null;
  loading: boolean;
  onSelect: (jobId: string) => void;
};

const BatchJobsNav = ({ jobs, activeJobId, loading, onSelect }: Props) => (
  <>
    <Typography
      variant="overline"
      sx={{ px: 2, color: 'text.secondary', fontWeight: 700, letterSpacing: 1 }}
    >
      Batch Jobs
    </Typography>
    <List dense>
      {loading &&
        [0, 1, 2].map((i) => (
          <Skeleton
            key={i}
            variant="text"
            width="80%"
            sx={{ mx: 2, my: 0.5 }}
          />
        ))}
      {!loading && jobs.length === 0 && (
        <Typography variant="body2" sx={{ px: 2, color: 'text.secondary' }}>
          No jobs found
        </Typography>
      )}
      {!loading &&
        jobs.map((jobId) => {
          const selected = jobId === activeJobId;
          return (
            <ListItemButton
              key={jobId}
              selected={selected}
              onClick={() => onSelect(jobId)}
              sx={{
                mx: 1,
                borderRadius: 1,
                '&.Mui-selected': {
                  bgcolor: 'rgba(21, 101, 192, 0.12)',
                  color: 'primary.dark',
                },
                '&.Mui-selected:hover': { bgcolor: 'rgba(21, 101, 192, 0.18)' },
              }}
            >
              <ListItemText
                primary={humanize(jobId)}
                slotProps={{
                  primary: {
                    sx: { fontWeight: selected ? 700 : 500, fontSize: 14 },
                  },
                }}
              />
            </ListItemButton>
          );
        })}
    </List>
  </>
);

export default BatchJobsNav;
