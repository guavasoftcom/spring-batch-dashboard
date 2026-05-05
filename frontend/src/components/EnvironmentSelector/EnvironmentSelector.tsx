import { useState } from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  List,
  ListItemButton,
  ListItemIcon,
  Menu,
  MenuItem,
  Select,
  Skeleton,
  Tooltip,
} from '@mui/material';
import { DatabaseIcon } from '~/components/DatabaseIcon';
import type { EnvironmentInfo } from '~/types';

type Props = {
  value: string;
  selectedType: string;
  options: EnvironmentInfo[];
  onChange: (value: string) => void;
  loading?: boolean;
  compact?: boolean;
};

const EnvironmentSelector = ({ value, selectedType, options, onChange, loading, compact }: Props) => {
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);

  if (loading) {
    return (
      <Box sx={{ px: compact ? 0 : 2, mb: 2, mt: 2, display: 'flex', justifyContent: 'center' }}>
        <Skeleton
          variant={compact ? 'circular' : 'rounded'}
          width={compact ? 28 : undefined}
          height={compact ? 28 : 40}
          sx={compact ? undefined : { width: '100%' }}
        />
      </Box>
    );
  }

  if (compact) {
    const disabled = options.length <= 1;
    const open = menuAnchor != null;
    return (
      <>
        <List sx={{ mt: 2 }}>
          <Tooltip title={value || 'Environment'} placement="right">
            <ListItemButton
              disabled={disabled}
              aria-label={value ? `Environment: ${value}` : 'Environment'}
              aria-haspopup="menu"
              aria-expanded={open ? 'true' : undefined}
              onClick={(e) => setMenuAnchor(e.currentTarget)}
              sx={{
                mx: 1,
                borderRadius: 1,
                justifyContent: 'center',
              }}
            >
              <ListItemIcon sx={{ minWidth: 0, color: 'inherit', justifyContent: 'center' }}>
                <DatabaseIcon type={selectedType} sx={{ fontSize: 26 }} />
              </ListItemIcon>
            </ListItemButton>
          </Tooltip>
        </List>
        <Menu
          anchorEl={menuAnchor}
          open={open}
          onClose={() => setMenuAnchor(null)}
          anchorOrigin={{ vertical: 'center', horizontal: 'right' }}
          transformOrigin={{ vertical: 'center', horizontal: 'left' }}
        >
          {options.map((env) => (
            <MenuItem
              key={env.name}
              selected={env.name === value}
              onClick={() => {
                setMenuAnchor(null);
                onChange(env.name);
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <DatabaseIcon type={env.type} fontSize="small" />
                <span>{env.name}</span>
              </Box>
            </MenuItem>
          ))}
        </Menu>
      </>
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
