import { Link as MuiLink, type SxProps, type Theme } from '@mui/material';
import DataThresholdingIcon from '@mui/icons-material/DataThresholding';

type Variant = 'cell' | 'large';

type Props = {
  executionId: number;
  onClick: (executionId: number) => void;
  variant?: Variant;
  sx?: SxProps<Theme>;
};

const VARIANTS: Record<Variant, { iconFontSize: number | 'small'; linkSx: SxProps<Theme> }> = {
  cell: {
    iconFontSize: 'small',
    linkSx: { fontWeight: 700 },
  },
  large: {
    iconFontSize: 32,
    linkSx: {
      mt: 1,
      fontWeight: 800,
      fontSize: '2.125rem',
      lineHeight: 1.235,
    },
  },
};

const ExecutionLink = ({ executionId, onClick, variant = 'cell', sx }: Props) => {
  const { iconFontSize, linkSx } = VARIANTS[variant];
  return (
    <MuiLink
      component="button"
      onClick={() => onClick(executionId)}
      sx={[
        {
          color: 'primary.dark',
          textDecoration: 'none',
          background: 'none',
          border: 0,
          p: 0,
          cursor: 'pointer',
          display: 'inline-flex',
          alignItems: 'center',
          gap: 0.75,
          '&:hover': { textDecoration: 'underline' },
        },
        linkSx,
        ...(Array.isArray(sx) ? sx : [sx]),
      ]}
    >
      {typeof iconFontSize === 'string' ? (
        <DataThresholdingIcon fontSize={iconFontSize} />
      ) : (
        <DataThresholdingIcon sx={{ fontSize: iconFontSize }} />
      )}
      #{executionId}
    </MuiLink>
  );
};

export default ExecutionLink;
