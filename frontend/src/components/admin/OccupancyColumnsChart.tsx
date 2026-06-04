import { Box, Stack, Tooltip, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { InventorySlot } from '../../types/catalog';

interface OccupancyColumnsChartProps {
  slots: InventorySlot[];
}

function formatSlotLabel(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    month: 'short',
  }).format(new Date(value));
}

function getOccupancy(slot: InventorySlot) {
  if (slot.capacity <= 0) return 0;

  return Math.min(100, Math.max(0, (slot.reserved / slot.capacity) * 100));
}

export function OccupancyColumnsChart({ slots }: OccupancyColumnsChartProps) {
  const { t } = useTranslation();

  if (slots.length === 0) {
    return (
      <Box
        sx={{
          alignItems: 'center',
          border: 1,
          borderColor: 'divider',
          borderRadius: 2,
          display: 'flex',
          minHeight: 220,
          justifyContent: 'center',
        }}
      >
        <Typography color="text.secondary">{t('admin.occupancy.empty')}</Typography>
      </Box>
    );
  }

  return (
    <Box
      aria-label={t('admin.occupancy.chartLabel')}
      role="img"
      sx={{
        border: 1,
        borderColor: 'divider',
        borderRadius: 2,
        overflowX: 'auto',
        p: 2,
      }}
    >
      <Stack direction="row" spacing={1} sx={{ alignItems: 'end', minHeight: 260, minWidth: slots.length * 28 }}>
        {slots.map((slot) => {
          const occupancy = getOccupancy(slot);
          const color = occupancy >= 85 ? 'error.main' : occupancy >= 65 ? 'secondary.main' : 'success.main';

          return (
            <Tooltip
              key={slot.slotStart}
              title={t('admin.occupancy.tooltip', {
                capacity: slot.capacity,
                free: slot.free,
                occupancy: Math.round(occupancy),
                reserved: slot.reserved,
                slot: formatSlotLabel(slot.slotStart),
              })}
            >
              <Box
                sx={{
                  alignItems: 'center',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 0.75,
                  width: 20,
                }}
              >
                <Box
                  sx={{
                    alignItems: 'end',
                    bgcolor: 'rgba(17, 24, 39, 0.08)',
                    borderRadius: 1,
                    display: 'flex',
                    height: 210,
                    overflow: 'hidden',
                    width: 16,
                  }}
                >
                  <Box
                    sx={{
                      bgcolor: color,
                      height: `${occupancy}%`,
                      transition: 'height 160ms ease',
                      width: '100%',
                    }}
                  />
                </Box>
                <Typography color="text.secondary" sx={{ fontSize: 10, writingMode: 'vertical-rl' }}>
                  {formatSlotLabel(slot.slotStart)}
                </Typography>
              </Box>
            </Tooltip>
          );
        })}
      </Stack>
    </Box>
  );
}
