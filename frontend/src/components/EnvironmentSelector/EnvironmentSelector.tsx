import { Box, FormControl, InputLabel, MenuItem, Select, Skeleton } from '@mui/material';

type Props = {
  value: string;
  options: string[];
  onChange: (value: string) => void;
  loading?: boolean;
};

const EnvironmentSelector = ({ value, options, onChange, loading }: Props) => {
  if (loading) {
    return (
      <Box sx={{ px: 2, mb: 2 }}>
        <Skeleton variant="rounded" height={40} />
      </Box>
    );
  }

  return (
    <FormControl size="small" sx={{ px: 2, mb: 2, width: '100%' }} disabled={options.length === 0}>
      <InputLabel id="env-select-label" sx={{ ml: 2 }}>Environment</InputLabel>
      <Select
        labelId="env-select-label"
        label="Environment"
        value={options.includes(value) ? value : ''}
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((opt) => (
          <MenuItem key={opt} value={opt}>{opt}</MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default EnvironmentSelector;
