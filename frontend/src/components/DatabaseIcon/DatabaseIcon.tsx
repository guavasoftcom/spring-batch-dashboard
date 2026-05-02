import { SvgIcon, type SvgIconProps } from '@mui/material';

const PALETTE: Record<string, { fill: string; label: string }> = {
  POSTGRESQL: { fill: '#336791', label: 'PG' },
  MYSQL: { fill: '#00618A', label: 'MY' },
  ORACLE: { fill: '#C74634', label: 'OR' },
};

type Props = SvgIconProps & { type: string };

const DatabaseIcon = ({ type, ...rest }: Props) => {
  const palette = PALETTE[type.toUpperCase()] ?? { fill: '#64748B', label: '?' };

  return (
    <SvgIcon viewBox="0 0 24 24" {...rest}>
      <path
        d="M12 3C7.6 3 4 4.3 4 6v12c0 1.7 3.6 3 8 3s8-1.3 8-3V6c0-1.7-3.6-3-8-3z"
        fill={palette.fill}
      />
      <ellipse cx="12" cy="6" rx="8" ry="1.6" fill="rgba(255,255,255,0.25)" />
      <text
        x="12"
        y="13.5"
        fontSize="7"
        fontWeight="800"
        fill="rgba(255,255,255,0.98)"
        textAnchor="middle"
        dominantBaseline="middle"
        fontFamily="Segoe UI, Helvetica, Arial, sans-serif"
      >
        {palette.label}
      </text>
    </SvgIcon>
  );
};

export default DatabaseIcon;
