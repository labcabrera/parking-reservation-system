import { Alert, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function AuthCallbackPage() {
  const { completeLogin } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    completeLogin()
      .then(() => navigate('/', { replace: true }))
      .catch((error: unknown) => {
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
