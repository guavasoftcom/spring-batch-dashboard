import { Box, Button, Stack, Typography } from '@mui/material';
import { ColorModeToggle } from '~/components';
import GitHubIcon from '~/pages/login/components/GitHubIcon';
import SpringLeafIcon from '~/pages/login/components/SpringLeafIcon';
import { appColors, pageGradient, useColorMode } from '~/theme';

type LoginPageProps = {
  checking: boolean;
  loginUrl: string;
};

const toggleSx = { position: 'absolute', top: 16, right: 16 } as const;

const LoginPage = ({ checking, loginUrl }: LoginPageProps) => {
  const { mode } = useColorMode();

  if (checking) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: pageGradient[mode],
          position: 'relative',
        }}
      >
        <ColorModeToggle sx={toggleSx} />
        <Typography variant="body1" color="text.secondary">
          Loading...
        </Typography>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        px: 3,
        background: pageGradient[mode],
        position: 'relative',
      }}
    >
      <ColorModeToggle sx={toggleSx} />
      <Box
        sx={{
          width: 'min(560px, 100%)',
          borderRadius: 2,
          p: { xs: 4, md: 6 },
          backgroundColor: 'background.paper',
          border: 1,
          borderColor: 'divider',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
        }}
      >
        <Stack spacing={3}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <SpringLeafIcon sx={{ color: appColors.leafGreen, fontSize: 30, flexShrink: 0 }} />
            <Typography
              component="h1"
              sx={{
                color: 'text.primary',
                lineHeight: 1.1,
                fontSize: { xs: '1.6rem', sm: '1.95rem' },
                whiteSpace: 'nowrap',
              }}
            >
              <Box component="span" sx={{ fontFamily: '"Trebuchet MS", "Segoe UI", sans-serif', fontWeight: 700 }}>
                Spring Batch
              </Box>
              <Box component="span" sx={{ ml: 1, fontFamily: '"Arial Black", "Segoe UI", sans-serif', fontWeight: 800 }}>
                Dashboard
              </Box>
            </Typography>
          </Box>
          <Button
            href={loginUrl}
            variant="contained"
            size="large"
            fullWidth
            startIcon={<GitHubIcon />}
            sx={{
              mt: 1,
              px: 3.5,
              py: 1.25,
              borderRadius: 1,
              fontWeight: 700,
              bgcolor: appColors.brandOrange,
              '&:hover': { bgcolor: appColors.brandOrangeDark },
            }}
          >
            Login with GitHub
          </Button>
        </Stack>
      </Box>
    </Box>
  );
};

export default LoginPage;
