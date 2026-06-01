import SwapVertIcon from '@mui/icons-material/SwapVert';
import { Box, Chip, Paper, Stack, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { FacilityResult } from '../../types/catalog';
import { FacilityResultCard } from './FacilityResultCard';
import { ParkingResultsMap } from './ParkingResultsMap';

interface ParkingResultsSectionProps {
  facilities: FacilityResult[];
  location: string;
  onSelectFacility: (facilityId: string) => void;
  reservationTimeLeft: number;
  selectedFacilityId: string | null;
}

export function ParkingResultsSection({
  facilities,
  location,
  onSelectFacility,
  reservationTimeLeft,
  selectedFacilityId,
}: ParkingResultsSectionProps) {
  return (
    <Box sx={{ bgcolor: 'background.paper', borderRadius: 3, p: { xs: 2, md: 3 } }}>
      <ParkingResultsHeader count={facilities.length} location={location} />
      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1.15fr) minmax(420px, 0.85fr)' },
        }}
      >
        <ParkingResultsList
          facilities={facilities}
          onSelectFacility={onSelectFacility}
          reservationTimeLeft={reservationTimeLeft}
          selectedFacilityId={selectedFacilityId}
        />
        <ParkingResultsMapPanel
          facilities={facilities}
          onSelectFacility={onSelectFacility}
          selectedFacilityId={selectedFacilityId}
        />
      </Box>
    </Box>
  );
}

function ParkingResultsHeader({ count, location }: { count: number; location: string }) {
  const { t } = useTranslation();

  return (
    <Stack
      direction={{ xs: 'column', md: 'row' }}
      spacing={2}
      sx={{ alignItems: { xs: 'flex-start', md: 'center' }, justifyContent: 'space-between', mb: 2.5 }}
    >
      <Typography variant="h2" sx={{ fontSize: { xs: 24, md: 28 }, fontWeight: 900 }}>
        {t('results.title', { count, location })}
      </Typography>
      <Chip
        icon={<SwapVertIcon />}
        label={t('results.sortRecommended')}
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
  );
}

function ParkingResultsList({
  facilities,
  onSelectFacility,
  reservationTimeLeft,
  selectedFacilityId,
}: Omit<ParkingResultsSectionProps, 'location'>) {
  return (
    <Stack spacing={2}>
      {facilities.map((facility) => (
        <FacilityResultCard
          facility={facility}
          isSelected={facility.facilityId === selectedFacilityId}
          key={facility.facilityId}
          onSelect={() => onSelectFacility(facility.facilityId)}
          reservationTimeLeft={reservationTimeLeft}
        />
      ))}
    </Stack>
  );
}

function ParkingResultsMapPanel({
  facilities,
  onSelectFacility,
  selectedFacilityId,
}: Pick<ParkingResultsSectionProps, 'facilities' | 'onSelectFacility' | 'selectedFacilityId'>) {
  return (
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
        facilities={facilities}
        onSelectFacility={onSelectFacility}
        selectedFacilityId={selectedFacilityId}
      />
    </Paper>
  );
}
