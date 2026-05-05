import { ChartsText } from '@mui/x-charts';
import type { ChartsTextProps } from '@mui/x-charts';

const EXECUTION_LABEL_PATTERN = /^#(\d+)$/;

/**
 * Custom axis-tick-label slot. Only the x-axis labels — which look like {@code #42} — get
 * cursor: pointer + an onClick that drills into the matching execution. The label is wrapped
 * in a {@code <g>} translated 6px down so it sits clear of the axis line; the wrapper also
 * carries the {@code execution-tick-label} className that the chart's sx hover rule targets
 * to underline the text on hover. Other tick labels (the y-axes) render as plain text since
 * their values don't match the pattern.
 */
export const createClickableTickLabel = (onRunClick: (executionId: number) => void) => {
  const ClickableTickLabel = (props: ChartsTextProps) => {
    const match = typeof props.text === 'string' ? EXECUTION_LABEL_PATTERN.exec(props.text) : null;
    if (!match) {
      return <ChartsText {...props} />;
    }
    const executionId = Number(match[1]);
    return (
      <g
        className="execution-tick-label"
        style={{ cursor: 'pointer' }}
        transform="translate(0, 6)"
        onClick={() => onRunClick(executionId)}
      >
        <ChartsText {...props} />
      </g>
    );
  };
  return ClickableTickLabel;
};
