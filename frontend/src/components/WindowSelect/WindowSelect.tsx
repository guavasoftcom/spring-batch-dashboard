import { FormControl, InputLabel, MenuItem, Select } from '@mui/material';
import { useWindow } from '~/shell/WindowContext';

export type WindowOption = { value: number; label: string };

export const WINDOW_OPTIONS: readonly WindowOption[] = [
  { value: 7, label: 'Last 7 days' },
  { value: 30, label: 'Last 30 days' },
  { value: 60, label: 'Last 60 days' },
  { value: 90, label: 'Last 90 days' },
] as const;

type Props = {
  /** Distinguishes the input/label pair when multiple selects share a page. */
  id?: string;
  label?: string;
  minWidth?: number;
};

const WindowSelect = ({ id = 'window-select', label = 'Window', minWidth = 160 }: Props) => {
  const { windowDays, setWindowDays } = useWindow();
  const labelId = `${id}-label`;
  return (
    <FormControl size="small" sx={{ minWidth }}>
      <InputLabel id={labelId}>{label}</InputLabel>
      <Select
        labelId={labelId}
        label={label}
        value={windowDays}
        onChange={(e) => setWindowDays(Number(e.target.value))}
      >
        {WINDOW_OPTIONS.map((opt) => (
          <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default WindowSelect;
