import { Box, Button, Stack, Typography } from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { ColorModeToggle } from '~/components';
import { BACKEND_BASE_URL } from '~/config/env';
import SpringLeafIcon from '~/pages/login/components/SpringLeafIcon';
import { appColors, pageGradient, useColorMode } from '~/theme';
import type { OAuth2Provider } from '~/types';

type LoginPageProps = {
  checking: boolean;
  providers: OAuth2Provider[];
};

const toggleSx = { position: 'absolute', top: 16, right: 16 } as const;

const LoginPage = ({ checking, providers }: LoginPageProps) => {
  const { mode } = useColorMode();

  const decorativeSx = {
    flex: 1,
    display: { xs: 'none', md: 'flex' },
    alignItems: 'center',
    justifyContent: 'center',
    px: 6,
    background: pageGradient[mode],
    position: 'relative',
    '&::before': {
      content: '""',
      position: 'absolute',
      inset: 0,
      backgroundImage: `url(/login-pattern-${mode}.png)`,
      backgroundRepeat: 'repeat',
      opacity: mode === 'light' ? 0.2 : 0.06,
      pointerEvents: 'none',
    },
  } as const;

  const titleColor = mode === 'dark' ? appColors.white : 'text.primary';

  const titleBlock = (compact = false) => (
    <Stack
      spacing={compact ? 1 : 1.5}
      sx={{
        position: 'relative',
        alignItems: compact ? 'center' : 'flex-start',
        textAlign: compact ? 'center' : 'left',
      }}
    >
      <Typography
        sx={{
          color: compact ? 'text.primary' : titleColor,
          opacity: 0.85,
          fontSize: compact ? { xs: '1rem', sm: '1.15rem' } : { md: '1.4rem', lg: '1.6rem' },
          fontWeight: 500,
          letterSpacing: 0.5,
        }}
      >
        Welcome back!
      </Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: compact ? 1 : 1.5 }}>
        <SpringLeafIcon
          sx={{
            color: appColors.leafGreen,
            fontSize: compact ? 30 : 44,
            flexShrink: 0,
          }}
        />
        <Typography
          component="h1"
          sx={{
            color: compact ? 'text.primary' : titleColor,
            lineHeight: 1.05,
            fontSize: compact ? { xs: '1.4rem', sm: '1.7rem' } : { md: '2.4rem', lg: '3rem' },
            whiteSpace: 'nowrap',
          }}
        >
          <Box
            component="span"
            sx={{ fontFamily: '"Trebuchet MS", "Segoe UI", sans-serif', fontWeight: 700 }}
          >
            Spring Batch
          </Box>
          {' '}
          <Box
            component="span"
            sx={{
              ml: compact ? 1 : 1.5,
              fontFamily: '"Arial Black", "Segoe UI", sans-serif',
              fontWeight: 800,
            }}
          >
            Dashboard
          </Box>
        </Typography>
      </Box>
    </Stack>
  );

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', position: 'relative' }}>
      <Box sx={decorativeSx}>{titleBlock()}</Box>
      <Box
        sx={{
          width: { xs: '100%', md: 480 },
          flexShrink: 0,
          backgroundColor: 'background.paper',
          boxShadow: { md: '-8px 0 24px rgba(0,0,0,0.18)' },
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          px: { xs: 4, md: 6 },
          py: 6,
          position: 'relative',
        }}
      >
        <ColorModeToggle sx={toggleSx} />
        {checking ? (
          <Typography variant="body1" color="text.secondary">
            Loading...
          </Typography>
        ) : (
          <Stack spacing={3} sx={{ width: '100%', maxWidth: 300 }}>
            <Box sx={{ display: { xs: 'block', md: 'none' }, mb: 1 }}>{titleBlock(true)}</Box>
            <Stack spacing={1} sx={{ alignItems: 'center' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, width: '100%' }}>
                <Box sx={{ flex: 1, height: '1px', bgcolor: 'divider' }} />
                <LockOutlinedIcon sx={{ color: 'text.primary', fontSize: 22 }} />
                <Box sx={{ flex: 1, height: '1px', bgcolor: 'divider' }} />
              </Box>
              <Typography
                component="h2"
                sx={{ color: 'text.primary', fontSize: '1.25rem', fontWeight: 700 }}
              >
                Login
              </Typography>
            </Stack>
            {providers.length === 0 ? (
              <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
                No sign-in providers are configured.
              </Typography>
            ) : (
              <Stack spacing={1.5}>
                {providers.map((provider) => {
                  const background = provider.color ?? appColors.brandOrange;
                  const hoverBackground = provider.color ?? appColors.brandOrangeDark;
                  const icon = provider.iconUrl ? (
                    <Box
                      component="img"
                      src={provider.iconUrl}
                      alt=""
                      sx={{ width: 22, height: 22 }}
                    />
                  ) : undefined;
                  return (
                    <Button
                      key={provider.id}
                      href={`${BACKEND_BASE_URL}${provider.loginUrl}`}
                      variant="contained"
                      size="large"
                      fullWidth
                      startIcon={icon}
                      sx={{
                        px: 3.5,
                        py: 1.25,
                        borderRadius: 1,
                        fontWeight: 700,
                        bgcolor: background,
                        '&:hover': { bgcolor: hoverBackground },
                      }}
                    >
                      Sign in with {provider.label}
                    </Button>
                  );
                })}
              </Stack>
            )}
          </Stack>
        )}
      </Box>
    </Box>
  );
};

export default LoginPage;
