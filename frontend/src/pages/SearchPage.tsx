import { useEffect, useState } from 'react';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import BlockIcon from '@mui/icons-material/Block';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import SearchIcon from '@mui/icons-material/Search';
import SwapVertIcon from '@mui/icons-material/SwapVert';
import SyncAltIcon from '@mui/icons-material/SyncAlt';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Collapse,
  IconButton,
  InputBase,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { DateTimePickerField } from '../components/forms/DateTimePickerField';
import { ParkingResultsMap } from '../components/results/ParkingResultsMap';
import { useParkingSearch } from '../hooks/useParkingSearch';
import type { FacilityResult, SearchRequest } from '../types/catalog';

const currencyFormatter = new Intl.NumberFormat('es-ES', {
  currency: 'EUR',
  maximumFractionDigits: 2,
  style: 'currency',
});

function formatPrice(value: number) {
  return currencyFormatter.format(value).replace(/\s/g, '');
}

function formatReservationTime(milliseconds: number) {
  const safeMilliseconds = Math.max(milliseconds, 0);
  const minutes = Math.floor(safeMilliseconds / 60_000);
  const seconds = Math.floor((safeMilliseconds % 60_000) / 1_000);

  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

function FacilityResultCard({
  facility,
  isSelected,
  onSelect,
  reservationTimeLeft,
}: {
  facility: FacilityResult;
  isSelected: boolean;
  onSelect: () => void;
  reservationTimeLeft: number;
}) {
  function handleKeyDown(event: React.KeyboardEvent<HTMLDivElement>) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onSelect();
    }
  }

  return (
    <Card
      aria-pressed={isSelected}
      onClick={onSelect}
      onKeyDown={handleKeyDown}
      role="button"
      tabIndex={0}
      sx={{
        borderColor: isSelected ? 'rgba(255, 121, 0, 0.55)' : 'rgba(17, 24, 39, 0.12)',
        boxShadow: 'none',
        cursor: 'pointer',
        overflow: 'hidden',
        transition: 'border-color 140ms ease, box-shadow 140ms ease, transform 140ms ease',
        '&:hover': {
          borderColor: 'rgba(255, 121, 0, 0.55)',
          boxShadow: '0 12px 34px rgba(17, 24, 39, 0.08)',
          transform: 'translateY(-1px)',
        },
        '&:focus-visible': {
          outline: '3px solid rgba(255, 121, 0, 0.35)',
          outlineOffset: 2,
        },
      }}
    >
      <CardContent sx={{ p: { xs: 2, md: 2.5 }, '&:last-child': { pb: { xs: 2, md: 2.5 } } }}>
        {facility.lowAvailabilityWarning && (
          <Box
            sx={{
              bgcolor: '#ffe1e6',
              color: '#a23b4b',
              display: 'inline-flex',
              fontSize: 12,
              fontWeight: 900,
              mb: 1.25,
              px: 1,
              py: 0.45,
              textTransform: 'uppercase',
            }}
          >
            Quedan pocas plazas para este parking
          </Box>
        )}
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          sx={{ justifyContent: 'space-between' }}
        >
          <Box>
            <Typography
              variant="h6"
              component="h2"
              sx={{
                color: isSelected ? 'secondary.main' : 'text.primary',
                fontSize: 18,
                fontWeight: 900,
              }}
            >
              {facility.name}
            </Typography>
            <Typography color="text.primary" sx={{ fontSize: 16 }}>
              {facility.address}, {facility.city}
            </Typography>
          </Box>

          <Box sx={{ minWidth: 150, textAlign: { xs: 'left', sm: 'right' } }}>
            <Typography color="primary" sx={{ fontSize: 18, fontWeight: 900 }}>
              {formatPrice(facility.dailyRate)} total
            </Typography>
            <Typography color="text.secondary" sx={{ fontSize: 12 }}>
              {formatPrice(facility.dailyRate)}/dia
            </Typography>
          </Box>
        </Stack>

        <Stack
          direction="row"
          sx={{
            alignItems: 'center',
            color: '#8a92a6',
            flexWrap: 'wrap',
            gap: { xs: 1.2, md: 2 },
            mt: 2.5,
          }}
        >
          <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
            <Typography sx={{ fontSize: 12, fontWeight: 800 }}>2h de cortesia</Typography>
          </Stack>
          <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
            <SyncAltIcon sx={{ fontSize: 17 }} />
            <Typography sx={{ fontSize: 12, fontWeight: 700 }}>Entradas y salidas ilimitadas</Typography>
          </Stack>
          {facility.tags.includes('EXPRESS_ENTRY') && (
            <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
              <BoltOutlinedIcon sx={{ fontSize: 17 }} />
              <Typography sx={{ fontSize: 12, fontWeight: 700 }}>Entrada express</Typography>
            </Stack>
          )}
          {facility.tags.includes('FREE_CANCELLATION') && (
            <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
              <BlockIcon sx={{ fontSize: 17 }} />
              <Typography sx={{ fontSize: 12, fontWeight: 700 }}>Cancelacion gratuita</Typography>
            </Stack>
          )}
          <Box sx={{ flexGrow: 1 }} />
          <Typography
            color={facility.availableSpots <= 5 ? 'secondary.main' : 'text.secondary'}
            sx={{ fontSize: 14, fontWeight: 800 }}
          >
            {facility.availableSpots} plazas
          </Typography>
        </Stack>
      </CardContent>
      <Collapse in={isSelected} timeout={180} unmountOnExit>
        <Box
          sx={{
            alignItems: { xs: 'stretch', sm: 'center' },
            bgcolor: '#f0e5df',
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 2,
            justifyContent: 'space-between',
            px: { xs: 2, md: 2.5 },
            py: 2,
          }}
        >
          <Typography sx={{ fontSize: 14 }}>
            Te reservamos estas opciones durante {formatReservationTime(reservationTimeLeft)} minutos.
          </Typography>
          <Button
            color="secondary"
            endIcon={<ArrowForwardIcon />}
            onClick={(event) => event.stopPropagation()}
            size="large"
            sx={{ borderRadius: 999, minWidth: { sm: 250 } }}
            variant="contained"
          >
            Elige este parking
          </Button>
        </Box>
      </Collapse>
    </Card>
  );
}

export default function SearchPage() {
  const [text, setText] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [submittedParams, setSubmittedParams] = useState<SearchRequest | null>(null);
  const [selectedFacilityId, setSelectedFacilityId] = useState<string | null>(null);
  const [reservationExpiresAt, setReservationExpiresAt] = useState<number | null>(null);
  const [now, setNow] = useState(Date.now());

  const { data, isLoading, isError, error } = useParkingSearch(submittedParams);

  useEffect(() => {
    if (!reservationExpiresAt) return undefined;

    const intervalId = window.setInterval(() => setNow(Date.now()), 1_000);

    return () => window.clearInterval(intervalId);
  }, [reservationExpiresAt]);

  useEffect(() => {
    setSelectedFacilityId(null);
    setReservationExpiresAt(null);
  }, [data]);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim() || !checkIn || !checkOut) return;
    setSubmittedParams({ q: text.trim(), checkIn, checkOut });
  }

  function handleSelectFacility(facilityId: string) {
    setSelectedFacilityId(facilityId);
    setReservationExpiresAt(Date.now() + 10 * 60_000);
    setNow(Date.now());
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
        <Box sx={{ bgcolor: 'background.paper', borderRadius: 3, p: { xs: 2, md: 3 } }}>
          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={2}
            sx={{ alignItems: { xs: 'flex-start', md: 'center' }, justifyContent: 'space-between', mb: 2.5 }}
          >
            <Typography variant="h2" sx={{ fontSize: { xs: 24, md: 28 }, fontWeight: 900 }}>
              {data.length} parkings cerca de {submittedParams?.q}
            </Typography>
            <Chip
              icon={<SwapVertIcon />}
              label="Ordenar: Recomendados"
              sx={{
                bgcolor: 'background.paper',
                border: 1,
                borderColor: 'divider',
                borderRadius: 999,
                fontWeight: 800,
                px: 1,
              }}
              variant="outlined"
            />
          </Stack>

          <Box
            sx={{
              display: 'grid',
              gap: 3,
              gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1.15fr) minmax(420px, 0.85fr)' },
            }}
          >
            <Stack spacing={2}>
              {data.map((facility) => (
                <FacilityResultCard
                  facility={facility}
                  isSelected={facility.facilityId === selectedFacilityId}
                  key={facility.facilityId}
                  onSelect={() => handleSelectFacility(facility.facilityId)}
                  reservationTimeLeft={reservationExpiresAt ? reservationExpiresAt - now : 0}
                />
              ))}
            </Stack>
            <Paper
              elevation={0}
              sx={{
                border: 1,
                borderColor: 'divider',
                borderRadius: 2,
                height: { xs: 420, lg: 'calc(100vh - 190px)' },
                minHeight: 560,
                overflow: 'hidden',
                position: { lg: 'sticky' },
                top: 96,
              }}
            >
              <ParkingResultsMap
                facilities={data}
                onSelectFacility={handleSelectFacility}
                selectedFacilityId={selectedFacilityId}
              />
            </Paper>
          </Box>
        </Box>
      )}

          <pre>
      {JSON.stringify(data, null, 2)}
    </pre>
    </Stack>
  );
}
