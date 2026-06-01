import { Box, Container, Link, Stack, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

export function AppFooter() {
  const { t } = useTranslation();

  return (
    <Box
      component="footer"
      sx={{
        borderTop: 1,
        borderColor: 'divider',
        bgcolor: 'background.paper',
        py: 3,
      }}
    >
      <Container maxWidth="lg">
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={1.5}
          sx={{ justifyContent: 'space-between' }}
        >
          <Typography variant="body2" color="text.secondary">
            {t('footer.product')}
          </Typography>
          <Stack direction="row" spacing={2}>
            <Link href="/" color="text.secondary" underline="hover">
              {t('footer.search')}
            </Link>
            <Typography variant="body2" color="text.secondary">
              v0.0.1
            </Typography>
          </Stack>
        </Stack>
      </Container>
    </Box>
  );
}
