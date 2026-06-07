import { Alert, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

let logoutCallbackPromise: Promise<void> | null = null;

export default function LogoutCallbackPage() {
  const { completeLogout } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    logoutCallbackPromise ??= completeLogout();

    logoutCallbackPromise
      .then(() => navigate('/', { replace: true }))
      .catch((error: unknown) => {
        logoutCallbackPromise = null;
        setErrorMessage(error instanceof Error ? error.message : t('auth.callback.logoutError'));
      });
  }, [completeLogout, navigate, t]);

  if (errorMessage) {
    return <Alert severity="error">{errorMessage}</Alert>;
  }

  return (
    <Stack spacing={2} sx={{ alignItems: 'center', py: 8 }}>
      <CircularProgress />
      <Typography color="text.secondary">{t('auth.callback.logout')}</Typography>
    </Stack>
  );
}
