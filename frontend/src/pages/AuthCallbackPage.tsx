import { Alert, CircularProgress, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function AuthCallbackPage() {
  const { completeLogin } = useAuth();
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    completeLogin()
      .then(() => navigate('/', { replace: true }))
      .catch((error: unknown) => {
        setErrorMessage(error instanceof Error ? error.message : 'No se pudo completar el login');
      });
  }, [completeLogin, navigate]);

  if (errorMessage) {
    return <Alert severity="error">{errorMessage}</Alert>;
  }

  return (
    <Stack spacing={2} sx={{ alignItems: 'center', py: 8 }}>
      <CircularProgress />
      <Typography color="text.secondary">Completando inicio de sesion...</Typography>
    </Stack>
  );
}
