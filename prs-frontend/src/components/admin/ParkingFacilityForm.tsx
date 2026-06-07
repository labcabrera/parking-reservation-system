import AddIcon from '@mui/icons-material/Add';
import {
  Button,
  Checkbox,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  FacilityStatus,
  FacilityTag,
  type CreateParkingFacilityRequest,
  type FacilityStatus as FacilityStatusValue,
  type FacilityTag as FacilityTagValue,
} from '../../types/catalog';

interface ParkingFacilityFormProps {
  isSubmitting: boolean;
  onSubmit: (request: CreateParkingFacilityRequest) => void;
}

const defaultFacility: CreateParkingFacilityRequest = {
  address: '',
  cancellationPolicy: {
    freeCancelHours: 24,
    penaltyCancelMinutes: 60,
  },
  city: '',
  location: {
    latitude: 40.4168,
    longitude: -3.7038,
  },
  name: '',
  pricingRule: {
    estimatedDailyPrice: 12,
    externalPricingId: '',
  },
  status: FacilityStatus.ACTIVE,
  tags: [],
  capacity: {
    longTerm: 40,
    shortTerm: 60,
    total: 100,
  },
};

const tagOptions = Object.values(FacilityTag);

export function ParkingFacilityForm({ isSubmitting, onSubmit }: ParkingFacilityFormProps) {
  const { t } = useTranslation();
  const [facility, setFacility] = useState<CreateParkingFacilityRequest>(defaultFacility);

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    onSubmit(facility);
  }

  function handleTagChange(event: SelectChangeEvent<FacilityTagValue[]>) {
    const value = event.target.value;
    setFacility((current) => ({
      ...current,
      tags: typeof value === 'string' ? (value.split(',') as FacilityTagValue[]) : value,
    }));
  }

  function updateCapacity(partialCapacity: Partial<CreateParkingFacilityRequest['capacity']>) {
    setFacility((current) => {
      const capacity = {
        ...current.capacity,
        ...partialCapacity,
      };

      return {
        ...current,
        capacity: {
          ...capacity,
          total: capacity.shortTerm + capacity.longTerm,
        },
      };
    });
  }

  return (
    <Stack component="form" spacing={2} onSubmit={handleSubmit}>
      <TextField
        label={t('admin.form.name')}
        onChange={(event) => setFacility((current) => ({ ...current, name: event.target.value }))}
        required
        value={facility.name}
      />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          label={t('admin.form.city')}
          onChange={(event) => setFacility((current) => ({ ...current, city: event.target.value }))}
          required
          value={facility.city}
        />
        <TextField
          fullWidth
          label={t('admin.form.address')}
          onChange={(event) => setFacility((current) => ({ ...current, address: event.target.value }))}
          required
          value={facility.address}
        />
      </Stack>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          label={t('admin.form.latitude')}
          onChange={(event) =>
            setFacility((current) => ({
              ...current,
              location: { ...current.location, latitude: Number(event.target.value) },
            }))
          }
          required
          slotProps={{ htmlInput: { max: 90, min: -90, step: 0.000001 } }}
          type="number"
          value={facility.location.latitude}
        />
        <TextField
          fullWidth
          label={t('admin.form.longitude')}
          onChange={(event) =>
            setFacility((current) => ({
              ...current,
              location: { ...current.location, longitude: Number(event.target.value) },
            }))
          }
          required
          slotProps={{ htmlInput: { max: 180, min: -180, step: 0.000001 } }}
          type="number"
          value={facility.location.longitude}
        />
      </Stack>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          label={t('admin.form.shortTermSpots')}
          onChange={(event) => updateCapacity({ shortTerm: Number(event.target.value) })}
          required
          slotProps={{ htmlInput: { min: 0 } }}
          type="number"
          value={facility.capacity.shortTerm}
        />
        <TextField
          fullWidth
          label={t('admin.form.longTermSpots')}
          onChange={(event) => updateCapacity({ longTerm: Number(event.target.value) })}
          required
          slotProps={{ htmlInput: { min: 0 } }}
          type="number"
          value={facility.capacity.longTerm}
        />
      </Stack>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          disabled
          label={t('admin.form.totalSpots')}
          slotProps={{ htmlInput: { min: 1 } }}
          type="number"
          value={facility.capacity.total}
        />
        <TextField
          fullWidth
          label={t('admin.form.dailyPrice')}
          onChange={(event) =>
            setFacility((current) => ({
              ...current,
              pricingRule: { ...current.pricingRule, estimatedDailyPrice: Number(event.target.value) },
            }))
          }
          required
          slotProps={{ htmlInput: { min: 0.01, step: 0.01 } }}
          type="number"
          value={facility.pricingRule.estimatedDailyPrice}
        />
      </Stack>
      <TextField
        label={t('admin.form.externalPricingId')}
        onChange={(event) =>
          setFacility((current) => ({
            ...current,
            pricingRule: { ...current.pricingRule, externalPricingId: event.target.value },
          }))
        }
        required
        value={facility.pricingRule.externalPricingId}
      />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          label={t('admin.form.freeCancelHours')}
          onChange={(event) =>
            setFacility((current) => ({
              ...current,
              cancellationPolicy: { ...current.cancellationPolicy, freeCancelHours: Number(event.target.value) },
            }))
          }
          slotProps={{ htmlInput: { min: 0 } }}
          type="number"
          value={facility.cancellationPolicy.freeCancelHours}
        />
        <TextField
          fullWidth
          label={t('admin.form.penaltyCancelMinutes')}
          onChange={(event) =>
            setFacility((current) => ({
              ...current,
              cancellationPolicy: { ...current.cancellationPolicy, penaltyCancelMinutes: Number(event.target.value) },
            }))
          }
          slotProps={{ htmlInput: { min: 0 } }}
          type="number"
          value={facility.cancellationPolicy.penaltyCancelMinutes}
        />
      </Stack>
      <FormControl fullWidth>
        <InputLabel id="parking-facility-status-label">{t('admin.form.status')}</InputLabel>
        <Select
          label={t('admin.form.status')}
          labelId="parking-facility-status-label"
          onChange={(event) =>
            setFacility((current) => ({ ...current, status: event.target.value as FacilityStatusValue }))
          }
          value={facility.status}
        >
          {Object.values(FacilityStatus).map((status) => (
            <MenuItem key={status} value={status}>
              {status}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl fullWidth>
        <InputLabel id="parking-facility-tags-label">{t('admin.form.tags')}</InputLabel>
        <Select
          label={t('admin.form.tags')}
          labelId="parking-facility-tags-label"
          multiple
          onChange={handleTagChange}
          renderValue={(selected) => selected.join(', ')}
          value={facility.tags}
        >
          {tagOptions.map((tag) => (
            <MenuItem key={tag} value={tag}>
              <FormControlLabel control={<Checkbox checked={facility.tags.includes(tag)} />} label={tag} />
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <Button disabled={isSubmitting} startIcon={<AddIcon />} type="submit" variant="contained">
        {t('admin.form.submit')}
      </Button>
    </Stack>
  );
}
