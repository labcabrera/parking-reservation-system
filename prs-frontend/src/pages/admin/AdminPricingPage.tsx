import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { createPricingRule, listPricingRules } from '../../services/admin/pricingApi';
import { BillingType, PricingRuleStatus, type CreatePricingRuleRequest, type PricingRule } from '../../types/pricing';
import { formatPrice } from '../../utils/formatters';

const defaultRule: CreatePricingRuleRequest = {
  baseRate: { amount: 12, currency: 'EUR' },
  billingType: BillingType.DAILY,
  dailyRate: { amount: 12, currency: 'EUR' },
  hourlyRate: { amount: 2, currency: 'EUR' },
  name: '',
  status: PricingRuleStatus.ACTIVE,
  taxRate: 0.21,
  validFrom: '',
  validTo: '',
};

export default function AdminPricingPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const rulesQuery = useQuery({
    queryFn: () => listPricingRules(0, 50),
    queryKey: ['pricing-rules', { page: 0, size: 50 }],
  });
  const createMutation = useMutation({
    mutationFn: (request: CreatePricingRuleRequest) => createPricingRule(request),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['pricing-rules'] });
    },
  });

  const rules = rulesQuery.data?.content ?? [];
  const activeRules = rules.filter((rule) => rule.status === PricingRuleStatus.ACTIVE).length;

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' } }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography component="h1" variant="h1">
            {t('admin.pricing.title')}
          </Typography>
          <Typography color="text.secondary">{t('admin.pricing.subtitle')}</Typography>
        </Box>
        <Button disabled={rulesQuery.isFetching} onClick={() => void rulesQuery.refetch()} startIcon={<RefreshIcon />} variant="outlined">
          {t('admin.refresh')}
        </Button>
      </Stack>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
        <MetricCard label={t('admin.pricing.metrics.rules')} value={String(rules.length)} />
        <MetricCard label={t('admin.pricing.metrics.active')} value={String(activeRules)} />
        <MetricCard label={t('admin.pricing.metrics.currency')} value={rules[0]?.baseRate.currency ?? 'EUR'} />
      </Stack>

      {rulesQuery.isError && <Alert severity="error">{rulesQuery.error.message}</Alert>}
      {createMutation.isError && <Alert severity="error">{createMutation.error.message}</Alert>}
      {createMutation.isSuccess && <Alert severity="success">{t('admin.pricing.form.created')}</Alert>}

      <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1.3fr) 420px' } }}>
        <PricingRulesTable isLoading={rulesQuery.isLoading} rules={rules} />
        <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, p: 2.5 }}>
          <Typography component="h2" variant="h2" sx={{ fontSize: 22, mb: 2 }}>
            {t('admin.pricing.form.title')}
          </Typography>
          <PricingRuleForm
            isSubmitting={createMutation.isPending}
            onSubmit={(request) => createMutation.mutate(request)}
          />
        </Paper>
      </Box>
    </Stack>
  );
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <Card sx={{ flex: 1 }}>
      <CardContent sx={{ '&:last-child': { pb: 2 }, p: 2 }}>
        <Typography color="text.secondary" sx={{ fontSize: 13, fontWeight: 700 }}>
          {label}
        </Typography>
        <Typography sx={{ fontSize: 28, fontWeight: 900 }}>{value}</Typography>
      </CardContent>
    </Card>
  );
}

function PricingRulesTable({ isLoading, rules }: { isLoading: boolean; rules: PricingRule[] }) {
  const { t } = useTranslation();

  return (
    <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, overflow: 'hidden' }}>
      <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'space-between', p: 2 }}>
        <Typography component="h2" variant="h2" sx={{ fontSize: 22 }}>
          {t('admin.pricing.rules.title')}
        </Typography>
        {isLoading && <CircularProgress size={22} />}
      </Box>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('admin.pricing.rules.name')}</TableCell>
              <TableCell>{t('admin.pricing.rules.billingType')}</TableCell>
              <TableCell align="right">{t('admin.pricing.rules.baseRate')}</TableCell>
              <TableCell align="right">{t('admin.pricing.rules.dailyRate')}</TableCell>
              <TableCell>{t('admin.pricing.rules.status')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rules.map((rule) => (
              <TableRow hover key={rule.id}>
                <TableCell>
                  <Typography sx={{ fontWeight: 800 }}>{rule.name}</Typography>
                  <Typography color="text.secondary" sx={{ fontSize: 12 }}>
                    {rule.id}
                  </Typography>
                </TableCell>
                <TableCell>{rule.billingType}</TableCell>
                <TableCell align="right">{formatPrice(rule.baseRate.amount)}</TableCell>
                <TableCell align="right">
                  {rule.dailyRate ? formatPrice(rule.dailyRate.amount) : t('results.priceUnavailable')}
                </TableCell>
                <TableCell>{rule.status ?? '-'}</TableCell>
              </TableRow>
            ))}
            {!isLoading && rules.length === 0 && (
              <TableRow>
                <TableCell colSpan={5}>
                  <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                    {t('admin.pricing.rules.empty')}
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}

function PricingRuleForm({
  isSubmitting,
  onSubmit,
}: {
  isSubmitting: boolean;
  onSubmit: (request: CreatePricingRuleRequest) => void;
}) {
  const { t } = useTranslation();
  const [rule, setRule] = useState<CreatePricingRuleRequest>(defaultRule);

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    onSubmit({
      ...rule,
      validFrom: rule.validFrom || null,
      validTo: rule.validTo || null,
    });
  }

  return (
    <Stack component="form" spacing={2} onSubmit={handleSubmit}>
      <TextField
        label={t('admin.pricing.form.name')}
        onChange={(event) => setRule((current) => ({ ...current, name: event.target.value }))}
        required
        value={rule.name}
      />
      <FormControl fullWidth>
        <InputLabel id="pricing-billing-type-label">{t('admin.pricing.form.billingType')}</InputLabel>
        <Select
          label={t('admin.pricing.form.billingType')}
          labelId="pricing-billing-type-label"
          onChange={(event) => setRule((current) => ({ ...current, billingType: event.target.value as BillingType }))}
          value={rule.billingType}
        >
          {Object.values(BillingType).map((billingType) => (
            <MenuItem key={billingType} value={billingType}>
              {billingType}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          label={t('admin.pricing.form.baseRate')}
          onChange={(event) =>
            setRule((current) => ({ ...current, baseRate: { ...current.baseRate, amount: Number(event.target.value) } }))
          }
          required
          slotProps={{ htmlInput: { min: 0, step: 0.01 } }}
          type="number"
          value={rule.baseRate.amount}
        />
        <TextField
          fullWidth
          label={t('admin.pricing.form.currency')}
          onChange={(event) =>
            setRule((current) => ({
              ...current,
              baseRate: { ...current.baseRate, currency: event.target.value },
              dailyRate: current.dailyRate ? { ...current.dailyRate, currency: event.target.value } : current.dailyRate,
              hourlyRate: current.hourlyRate ? { ...current.hourlyRate, currency: event.target.value } : current.hourlyRate,
            }))
          }
          required
          value={rule.baseRate.currency}
        />
      </Stack>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <TextField
          fullWidth
          label={t('admin.pricing.form.hourlyRate')}
          onChange={(event) =>
            setRule((current) => ({
              ...current,
              hourlyRate: { amount: Number(event.target.value), currency: current.baseRate.currency },
            }))
          }
          slotProps={{ htmlInput: { min: 0, step: 0.01 } }}
          type="number"
          value={rule.hourlyRate?.amount ?? ''}
        />
        <TextField
          fullWidth
          label={t('admin.pricing.form.dailyRate')}
          onChange={(event) =>
            setRule((current) => ({
              ...current,
              dailyRate: { amount: Number(event.target.value), currency: current.baseRate.currency },
            }))
          }
          slotProps={{ htmlInput: { min: 0, step: 0.01 } }}
          type="number"
          value={rule.dailyRate?.amount ?? ''}
        />
      </Stack>
      <TextField
        helperText={t('admin.pricing.form.taxRateHelp')}
        label={t('admin.pricing.form.taxRate')}
        onChange={(event) => setRule((current) => ({ ...current, taxRate: Number(event.target.value) }))}
        slotProps={{ htmlInput: { max: 1, min: 0, step: 0.0001 } }}
        type="number"
        value={rule.taxRate ?? ''}
      />
      <Button disabled={isSubmitting} startIcon={<AddIcon />} type="submit" variant="contained">
        {t('admin.pricing.form.submit')}
      </Button>
    </Stack>
  );
}
