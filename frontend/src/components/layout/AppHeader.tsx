import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
import LoginIcon from '@mui/icons-material/Login';
import LogoutIcon from '@mui/icons-material/Logout';
import {
  AppBar,
  Box,
  Button,
  Container,
  Link,
  ToggleButton,
  ToggleButtonGroup,
  Stack,
  Toolbar,
  Tooltip,
  Typography,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { supportedLanguages, type SupportedLanguage } from '../../i18n';

function getUserDisplayName(profileName: string | undefined, email: string | undefined, fallback: string) {
  return profileName ?? email ?? fallback;
}

export function AppHeader() {
  const { isAuthenticated, isConfigured, isLoading, login, logout, user } = useAuth();
  const { i18n, t } = useTranslation();
  const displayName = getUserDisplayName(user?.profile.name, user?.profile.email, t('auth.defaultUser'));

  function handleLanguageChange(_: React.MouseEvent<HTMLElement>, language: SupportedLanguage | null) {
    if (!language) return;

    void i18n.changeLanguage(language);
    window.localStorage.setItem('language', language);
  }

  return (
    <AppBar
      component="header"
      position="sticky"
      color="inherit"
      elevation={0}
      sx={{
        backdropFilter: 'blur(18px)',
        bgcolor: 'rgba(255, 255, 255, 0.94)',
        borderBottom: 1,
        borderColor: 'rgba(17, 24, 39, 0.08)',
      }}
    >
      <Container maxWidth="lg">
        <Toolbar disableGutters sx={{ minHeight: { xs: 68, sm: 80 }, gap: 3 }}>
          <Stack
            component={RouterLink}
            to="/"
            direction="row"
            spacing={1.5}
            sx={{ alignItems: 'center', color: 'primary.main', textDecoration: 'none' }}
          >
            <Box
              sx={{
                alignItems: 'center',
                bgcolor: 'secondary.main',
                borderRadius: 2,
                color: 'secondary.contrastText',
                display: 'flex',
                height: 44,
                justifyContent: 'center',
                width: 44,
              }}
            >
              <DirectionsCarFilledIcon aria-hidden />
            </Box>
            <Box>
              <Typography
                variant="h6"
                component="p"
                sx={{ letterSpacing: 0, lineHeight: 1, fontWeight: 900 }}
              >
                {t('header.brand')}
              </Typography>
              <Typography
                variant="caption"
                sx={{ color: 'text.secondary', fontWeight: 700, letterSpacing: 0.4 }}
              >
                {t('header.subtitle')}
              </Typography>
            </Box>
          </Stack>

          <Stack
            component="nav"
            direction="row"
            spacing={3}
            sx={{ display: { xs: 'none', md: 'flex' } }}
          >
            {[
              t('header.nav.parkings'),
              t('header.nav.bookings'),
              t('header.nav.business'),
              t('header.nav.support'),
            ].map((item) => (
              <Link
                key={item}
                color="text.primary"
                href="/"
                sx={{ fontSize: 14, fontWeight: 700 }}
                underline="none"
              >
                {item}
              </Link>
            ))}
          </Stack>

          <Box sx={{ flexGrow: 1 }} />

          <ToggleButtonGroup
            exclusive
            onChange={handleLanguageChange}
            size="small"
            value={i18n.resolvedLanguage}
            sx={{
              '& .MuiToggleButton-root': {
                border: 0,
                color: 'text.secondary',
                fontSize: 12,
                fontWeight: 900,
                px: 1,
              },
              '& .Mui-selected': {
                bgcolor: 'transparent',
                color: 'secondary.main',
              },
            }}
          >
            {supportedLanguages.map((language) => (
              <ToggleButton aria-label={language.label} key={language.code} value={language.code}>
                {language.label}
              </ToggleButton>
            ))}
          </ToggleButtonGroup>

          {isAuthenticated ? (
            <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{ display: { xs: 'none', sm: 'block' } }}
              >
                {displayName}
              </Typography>
              <Button
                color="secondary"
                startIcon={<LogoutIcon />}
                variant="outlined"
                onClick={() => void logout()}
                sx={{ borderRadius: 999 }}
              >
                {t('auth.logout')}
              </Button>
            </Stack>
          ) : (
            <Tooltip
              title={
                isConfigured
                  ? t('auth.loginTooltip')
                  : t('auth.missingConfig')
              }
            >
              <span>
                <Button
                  color="secondary"
                  disabled={!isConfigured || isLoading}
                  startIcon={<LoginIcon />}
                  variant="contained"
                  onClick={() => void login()}
                  sx={{ borderRadius: 999, px: 2.5 }}
                >
                  {t('auth.login')}
                </Button>
              </span>
            </Tooltip>
          )}
        </Toolbar>
      </Container>
    </AppBar>
  );
}
