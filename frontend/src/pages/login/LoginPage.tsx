import { Box, Button, Stack, Typography } from '@mui/material';
import GitHubIcon from '~/pages/login/components/GitHubIcon';
import SpringLeafIcon from '~/pages/login/components/SpringLeafIcon';

type LoginPageProps = {
  checking: boolean;
  loginUrl: string;
};

const LoginPage = ({ checking, loginUrl }: LoginPageProps) => {
  if (checking) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
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
        background:
          'radial-gradient(circle at 12% 18%, rgba(245, 124, 0, 0.24), transparent 40%), radial-gradient(circle at 88% 80%, rgba(21, 101, 192, 0.3), transparent 45%), linear-gradient(140deg, #031626 0%, #0A2D4A 50%, #123A5C 100%)',
      }}
    >
      <Box
        sx={{
          width: 'min(560px, 100%)',
          borderRadius: 2,
          p: { xs: 4, md: 6 },
          backdropFilter: 'blur(8px)',
          backgroundColor: 'rgba(255,255,255,0.08)',
          border: '1px solid rgba(255,255,255,0.18)',
          boxShadow: '0 24px 80px rgba(0, 0, 0, 0.35)',
        }}
      >
        <Stack spacing={3}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <SpringLeafIcon sx={{ color: '#7FD36E', fontSize: 30, flexShrink: 0 }} />
            <Typography
              component="h1"
              sx={{
                color: '#EAF4FF',
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
              bgcolor: '#F57C00',
              '&:hover': { bgcolor: '#BB4D00' },
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
