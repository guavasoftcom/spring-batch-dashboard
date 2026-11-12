import { useTheme } from '@mui/material/styles';

type Props = {
  /** Daily average durations, ascending by date. */
  points: { date: string; averageSeconds: number }[];
  /** Highlights the line in the warning colour, e.g. when the job is slower than usual. */
  warn?: boolean;
  label: string;
};

const WIDTH = 120;
const HEIGHT = 28;
const PAD = 3;

const toPath = (coords: [number, number][]) =>
  coords.map(([x, y], index) => `${index === 0 ? 'M' : 'L'}${x.toFixed(1)} ${y.toFixed(1)}`).join(' ');

/**
 * Tiny line of a job's daily average duration. Scaled to the job's own range so a 30-second
 * job and a 2-hour job are both readable; x is positioned by date so days without runs show as gaps.
 */
const Sparkline = ({ points, warn = false, label }: Props) => {
  const theme = useTheme();
  const stroke = warn ? theme.palette.warning.main : theme.palette.primary.main;

  if (points.length === 0) {
    return <span aria-label="No duration data">—</span>;
  }

  const times = points.map((point) => Date.parse(point.date));
  const firstTime = times[0];
  const span = times[times.length - 1] - firstTime;
  const maxSeconds = Math.max(...points.map((point) => point.averageSeconds), 1);
  const baseline = HEIGHT - PAD;
  const coords = points.map((point, index): [number, number] => [
    span === 0 ? WIDTH / 2 : PAD + ((times[index] - firstTime) / span) * (WIDTH - 2 * PAD),
    baseline - (point.averageSeconds / maxSeconds) * (baseline - PAD),
  ]);
  const [endX, endY] = coords[coords.length - 1];

  return (
    <svg width={WIDTH} height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label={label}>
      {coords.length > 1 && (
        <>
          <path
            d={`${toPath(coords)} L${endX.toFixed(1)} ${baseline} L${coords[0][0].toFixed(1)} ${baseline} Z`}
            fill={stroke}
            opacity={0.14}
          />
          <path d={toPath(coords)} fill="none" stroke={stroke} strokeWidth={1.5} strokeLinejoin="round" strokeLinecap="round" />
        </>
      )}
      <circle cx={endX} cy={endY} r={2.6} fill={stroke} />
    </svg>
  );
};

export default Sparkline;
