import { useState } from 'react';
import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import SearchIcon from '@mui/icons-material/Search';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  IconButton,
  InputBase,
  Stack,
  Typography,
} from '@mui/material';
import { DateTimePickerField } from '../components/forms/DateTimePickerField';
import { useParkingSearch } from '../hooks/useParkingSearch';
import type { FacilityResult, SearchRequest } from '../types/catalog';

function FacilityResultCard({ facility }: { facility: FacilityResult }) {
  return (
    <Card>
      <CardContent>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          sx={{ justifyContent: 'space-between' }}
        >
          <Box>
            <Typography variant="h6" component="h2">
              {facility.name}
            </Typography>
            <Typography color="text.secondary">
              {facility.city} - {facility.address}
            </Typography>
          </Box>

          {facility.lowAvailabilityWarning && (
            <Chip
              color="warning"
              icon={<WarningAmberIcon />}
              label="Poca disponibilidad"
              variant="outlined"
            />
          )}
        </Stack>

        <Typography sx={{ mt: 2 }}>
          Plazas disponibles: <strong>{facility.availableSpots}</strong>
        </Typography>

        <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1, mt: 2 }}>
          {facility.tags.map((tag) => (
            <Chip key={tag} label={tag.replace(/_/g, ' ')} size="small" />
          ))}
        </Stack>
      </CardContent>
    </Card>
  );
}

export default function SearchPage() {
  const [text, setText] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [submittedParams, setSubmittedParams] = useState<SearchRequest | null>(null);

  const { data, isLoading, isError, error } = useParkingSearch(submittedParams);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim() || !checkIn || !checkOut) return;
    setSubmittedParams({ q: text.trim(), checkIn, checkOut });
  }

  return (
    <Stack spacing={4}>
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

          <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{
              bgcolor: 'background.paper',
              borderRadius: { xs: 4, md: 999 },
              boxShadow: '0 28px 80px rgba(0, 0, 0, 0.28)',
              color: 'text.primary',
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', md: '1.2fr 1fr 1fr 112px' },
              maxWidth: 1060,
              overflow: 'visible',
            }}
          >
            <Box
              sx={{
                alignItems: 'center',
                borderBottom: { xs: 1, md: 0 },
                borderColor: 'divider',
                borderRight: { md: 1 },
                display: 'flex',
                gap: 1.5,
                px: { xs: 2.5, md: 3 },
                py: 2,
              }}
            >
              <LocationOnOutlinedIcon sx={{ color: 'text.secondary', fontSize: 22 }} />
              <Box sx={{ minWidth: 0, width: '100%' }}>
                <Typography color="text.secondary" variant="caption">
                  ¿A donde vas?
                </Typography>
                <InputBase
                  fullWidth
                  inputProps={{ 'aria-label': 'Ubicacion' }}
                  onChange={(e) => setText(e.target.value)}
                  placeholder="Madrid, Spain"
                  required
                  sx={{ fontSize: 17, lineHeight: 1.2 }}
                  value={text}
                />
              </Box>
            </Box>

            <Box sx={{ borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', borderRight: { md: 1 } }}>
              <DateTimePickerField
                helperText="Entrada"
                label="¿Cuando llegas?"
                onChange={setCheckIn}
                value={checkIn}
              />
            </Box>
            <Box sx={{ borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', borderRight: { md: 1 } }}>
              <DateTimePickerField
                helperText="Salida"
                label="¿Cuando te vas?"
                onChange={setCheckOut}
                value={checkOut}
              />
            </Box>
            <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'center', p: { xs: 2, md: 1.5 } }}>
              <IconButton
                aria-label="Buscar parking"
                disabled={isLoading}
                type="submit"
                sx={{
                  bgcolor: 'secondary.main',
                  color: 'secondary.contrastText',
                  height: 56,
                  width: 56,
                  '&:hover': { bgcolor: 'secondary.dark' },
                }}
              >
                {isLoading ? <CircularProgress color="inherit" size={22} /> : <SearchIcon />}
              </IconButton>
            </Box>
          </Box>
        </Stack>
      </Box>

      {isError && (
        <Alert severity="error">{error?.message ?? 'No se pudieron cargar los resultados'}</Alert>
      )}

      {data && data.length === 0 && !isLoading && (
        <Alert severity="info">No se encontraron parkings para la busqueda.</Alert>
      )}

      {data && data.length > 0 && (
        <Stack spacing={2}>
          <Typography color="text.secondary">{data.length} resultado(s)</Typography>
          {data.map((facility) => (
            <FacilityResultCard key={facility.facilityId} facility={facility} />
          ))}
        </Stack>
      )}
    </Stack>
  );
}
