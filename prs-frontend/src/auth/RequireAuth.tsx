import { Alert, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useRef, useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from './AuthContext';

let authRedirectInProgress = false;

export function RequireAuth({ children }: { children: ReactNode }) {
  const { isAuthenticated, isConfigured, isLoading, login } = useAuth();
  const { t } = useTranslation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const hasStartedLogin = useRef(false);

  useEffect(() => {
    if (isLoading || isAuthenticated || !isConfigured) {
      return;
    }

    if (hasStartedLogin.current || authRedirectInProgress) {
      return;
    }

    hasStartedLogin.current = true;
    authRedirectInProgress = true;
    login().catch((error: unknown) => {
      setErrorMessage(error instanceof Error ? error.message : t('auth.callback.loginError'));
      hasStartedLogin.current = false;
      authRedirectInProgress = false;
    });
  }, [isAuthenticated, isConfigured, isLoading, login, t]);

  if (!isConfigured) {
    return <Alert severity="warning">{t('auth.missingConfig')}</Alert>;
  }

  if (errorMessage) {
    return <Alert severity="error">{errorMessage}</Alert>;
  }

  if (isLoading || !isAuthenticated) {
    return (
      <Stack spacing={2} sx={{ alignItems: 'center', py: 8 }}>
        <CircularProgress />
        <Typography color="text.secondary">{t('auth.callback.login')}</Typography>
      </Stack>
    );
  }

  return <>{children}</>;
}
