import { IconButton, Tooltip, type SxProps, type Theme } from '@mui/material';
import DarkModeOutlinedIcon from '@mui/icons-material/DarkModeOutlined';
import LightModeOutlinedIcon from '@mui/icons-material/LightModeOutlined';
import { useColorMode } from '~/theme';

type Props = {
  sx?: SxProps<Theme>;
};

const ColorModeToggle = ({ sx }: Props) => {
  const { mode, toggleMode } = useColorMode();
  return (
    <Tooltip title={mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}>
      <IconButton
        onClick={toggleMode}
        aria-label="toggle color mode"
        sx={[{ color: 'text.primary' }, ...(Array.isArray(sx) ? sx : [sx])]}
      >
        {mode === 'dark' ? <LightModeOutlinedIcon /> : <DarkModeOutlinedIcon />}
      </IconButton>
    </Tooltip>
  );
};

export default ColorModeToggle;
