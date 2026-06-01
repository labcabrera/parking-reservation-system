import { Box, Stack, Typography } from '@mui/material';
import type { ReactNode } from 'react';

export function SearchHero({ children }: { children: ReactNode }) {
  return (
    <Box
      sx={{
        bgcolor: 'primary.main',
        borderRadius: 3,
        color: 'primary.contrastText',
        mb: { xs: 1, md: 3 },
        minHeight: { xs: 430, md: 500 },
        overflow: 'visible',
        px: { xs: 2, md: 6 },
        py: { xs: 4, md: 7 },
        position: 'relative',
        '&::after': {
          background:
            'linear-gradient(135deg, rgba(255, 121, 0, 0.34), rgba(255, 255, 255, 0.05)), repeating-linear-gradient(115deg, transparent 0 34px, rgba(255,255,255,0.06) 34px 36px)',
          borderRadius: 3,
          content: '""',
          inset: 0,
          opacity: 0.8,
          position: 'absolute',
        },
      }}
    >
      <Stack spacing={4} sx={{ position: 'relative', zIndex: 1 }}>
        <Box sx={{ maxWidth: 650, pt: { md: 6 } }}>
          <Typography variant="h1" sx={{ color: 'inherit', fontSize: { xs: 36, md: 56 }, mb: 2 }}>
            Reserva justo la plaza que necesitas
          </Typography>
          <Typography sx={{ color: 'rgba(255, 255, 255, 0.78)', fontSize: { xs: 17, md: 20 } }}>
            Aparcamientos urbanos, reservas para empresas y control de disponibilidad en tiempo real.
          </Typography>
        </Box>
        {children}
      </Stack>
    </Box>
  );
}
