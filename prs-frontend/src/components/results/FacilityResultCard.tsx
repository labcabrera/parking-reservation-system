import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import BlockIcon from '@mui/icons-material/Block';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import SyncAltIcon from '@mui/icons-material/SyncAlt';
import { Box, Button, Card, CardContent, CircularProgress, Collapse, Stack, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { FacilityResult } from '../../types/catalog';
import { formatPrice, formatReservationTime } from '../../utils/formatters';

interface FacilityResultCardProps {
  facility: FacilityResult;
  isSelected: boolean;
  isStartingReservation: boolean;
  onSelect: () => void;
  onStartReservation: () => void;
  reservationTimeLeft: number;
}

export function FacilityResultCard({
  facility,
  isSelected,
  isStartingReservation,
  onSelect,
  onStartReservation,
  reservationTimeLeft,
}: FacilityResultCardProps) {
  const { t } = useTranslation();

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
        <LowAvailabilityLabel isVisible={facility.lowAvailability ?? facility.lowAvailabilityWarning ?? false} label={t('results.lowAvailability')} />
        <FacilitySummary facility={facility} isSelected={isSelected} />
        <FacilityBenefits facility={facility} />
      </CardContent>
      <SelectionPanel
        isOpen={isSelected}
        isStartingReservation={isStartingReservation}
        onStartReservation={onStartReservation}
        reservationTimeLeft={reservationTimeLeft}
      />
    </Card>
  );
}

function LowAvailabilityLabel({ isVisible, label }: { isVisible: boolean; label: string }) {
  if (!isVisible) return null;

  return (
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
      {label}
    </Box>
  );
}

function FacilitySummary({ facility, isSelected }: { facility: FacilityResult; isSelected: boolean }) {
  const { t } = useTranslation();
  const displayAmount = facility.estimatedPrice?.amount ?? facility.dailyRate;
  const displayCurrency = facility.estimatedPrice?.currency ?? facility.currency;
  const price = displayAmount == null ? undefined : formatPrice(displayAmount);
  const dailyRate = facility.dailyRate == null ? undefined : formatPrice(facility.dailyRate);

  return (
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
          {price ? t('results.totalPrice', { price, currency: displayCurrency }) : t('results.priceUnavailable')}
        </Typography>
        {dailyRate && (
          <Typography color="text.secondary" sx={{ fontSize: 12 }}>
            {t('results.dayPrice', { currency: facility.currency, price: dailyRate })}
          </Typography>
        )}
      </Box>
    </Stack>
  );
}

function FacilityBenefits({ facility }: { facility: FacilityResult }) {
  const { t } = useTranslation();

  return (
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
      <Benefit label={t('results.benefits.courtesy')} />
      <Benefit icon={<SyncAltIcon sx={{ fontSize: 17 }} />} label={t('results.benefits.unlimited')} />
      {facility.tags.includes('EXPRESS_ENTRY') && (
        <Benefit icon={<BoltOutlinedIcon sx={{ fontSize: 17 }} />} label={t('results.benefits.express')} />
      )}
      {facility.tags.includes('FREE_CANCELLATION') && (
        <Benefit icon={<BlockIcon sx={{ fontSize: 17 }} />} label={t('results.benefits.freeCancellation')} />
      )}
      <Box sx={{ flexGrow: 1 }} />
      <Typography
        color={facility.availableSpots <= 5 ? 'secondary.main' : 'text.secondary'}
        sx={{ fontSize: 14, fontWeight: 800 }}
      >
        {t('results.spots', { count: facility.availableSpots })}
      </Typography>
    </Stack>
  );
}

function Benefit({ icon, label }: { icon?: React.ReactNode; label: string }) {
  return (
    <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center' }}>
      {icon}
      <Typography sx={{ fontSize: 12, fontWeight: icon ? 700 : 800 }}>{label}</Typography>
    </Stack>
  );
}

function SelectionPanel({
  isOpen,
  isStartingReservation,
  onStartReservation,
  reservationTimeLeft,
}: {
  isOpen: boolean;
  isStartingReservation: boolean;
  onStartReservation: () => void;
  reservationTimeLeft: number;
}) {
  const { t } = useTranslation();

  function handleStartReservation(event: React.MouseEvent<HTMLButtonElement>) {
    event.stopPropagation();
    onStartReservation();
  }

  return (
    <Collapse in={isOpen} timeout={180} unmountOnExit>
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
          {t('results.reserveHold', { time: formatReservationTime(reservationTimeLeft) })}
        </Typography>
        <Button
          color="secondary"
          disabled={isStartingReservation}
          endIcon={
            isStartingReservation ? <CircularProgress color="inherit" size={18} /> : <ArrowForwardIcon />
          }
          onClick={handleStartReservation}
          size="large"
          sx={{ borderRadius: 999, minWidth: { sm: 250 } }}
          variant="contained"
        >
          {isStartingReservation ? t('results.startingReservation') : t('results.choose')}
        </Button>
      </Box>
    </Collapse>
  );
}
