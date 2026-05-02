import { Box, FormControl, InputLabel, MenuItem, Select, Skeleton, Tooltip } from '@mui/material';
import { DatabaseIcon } from '~/components/DatabaseIcon';
import type { EnvironmentInfo } from '~/types';

type Props = {
  value: string;
  selectedType: string;
  options: EnvironmentInfo[];
  onChange: (value: string) => void;
  loading?: boolean;
};

const EnvironmentSelector = ({ value, selectedType, options, onChange, loading }: Props) => {
  if (loading) {
    return (
      <Box sx={{ px: 2, mb: 2, mt: 2 }}>
        <Skeleton variant="rounded" height={40} />
      </Box>
    );
  }

  return (
    <FormControl size="small" sx={{ px: 2, mb: 2, mt: 2, width: '100%' }} disabled={options.length === 0}>
      <InputLabel id="env-select-label" sx={{ ml: 2 }}>Environment</InputLabel>
      <Select
        labelId="env-select-label"
        label="Environment"
        readOnly={options?.length <= 1}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        renderValue={(selected) => (
          <Tooltip title={selected} placement="top">
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 0 }}>
              <DatabaseIcon type={selectedType} fontSize="small" />
              <Box
                component="span"
                sx={{
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  minWidth: 0,
                }}
              >
                {selected}
              </Box>
            </Box>
          </Tooltip>
        )}
      >
        {options.map((env) => (
          <MenuItem key={env.name} value={env.name}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <DatabaseIcon type={env.type} fontSize="small" />
              <span>{env.name}</span>
            </Box>
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default EnvironmentSelector;
