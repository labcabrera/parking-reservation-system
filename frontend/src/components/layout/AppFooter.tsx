import { Box, Container, Link, Stack, Typography } from '@mui/material';

export function AppFooter() {
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
            Parking Reservation System
          </Typography>
          <Stack direction="row" spacing={2}>
            <Link href="/" color="text.secondary" underline="hover">
              Busqueda
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
