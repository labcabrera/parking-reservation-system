import { Alert, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { consumeRememberedRoute, getSafeReturnTo } from '../auth/authRedirect';

let loginCallbackPromise: Promise<string> | null = null;

export default function AuthCallbackPage() {
  const { completeLogin } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    loginCallbackPromise ??= completeLogin().then((user) => {
      const state = user.state as { returnTo?: string } | undefined;
      return consumeRememberedRoute(getSafeReturnTo(state?.returnTo));
    });

    loginCallbackPromise
      .then((returnTo) => navigate(returnTo, { replace: true }))
      .catch((error: unknown) => {
        loginCallbackPromise = null;
        setErrorMessage(error instanceof Error ? error.message : t('auth.callback.loginError'));
      });
  }, [completeLogin, navigate, t]);

  if (errorMessage) {
    return <Alert severity="error">{errorMessage}</Alert>;
  }

  return (
    <Stack spacing={2} sx={{ alignItems: 'center', py: 8 }}>
      <CircularProgress />
      <Typography color="text.secondary">{t('auth.callback.login')}</Typography>
    </Stack>
  );
}
