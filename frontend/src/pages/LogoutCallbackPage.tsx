import { Alert, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function LogoutCallbackPage() {
  const { completeLogout } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    completeLogout()
      .then(() => navigate('/', { replace: true }))
      .catch((error: unknown) => {
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
