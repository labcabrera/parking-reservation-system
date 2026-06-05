import LocalParkingIcon from '@mui/icons-material/LocalParking';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import RuleIcon from '@mui/icons-material/Rule';
import { Box, Card, CardActionArea, CardContent, Stack, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { Link as RouterLink } from 'react-router-dom';

const adminAreas = [
  {
    descriptionKey: 'admin.home.facilitiesDescription',
    icon: <LocalParkingIcon />,
    titleKey: 'admin.home.facilities',
    to: '/admin/facilities',
  },
  {
    descriptionKey: 'admin.home.pricingDescription',
    icon: <RuleIcon />,
    titleKey: 'admin.home.pricing',
    to: '/admin/pricing',
  },
  {
    descriptionKey: 'admin.home.operationsDescription',
    icon: <ReceiptLongIcon />,
    titleKey: 'admin.home.operations',
    to: '/admin/operations',
  },
];

export default function AdminHomePage() {
  const { t } = useTranslation();

  return (
    <Stack spacing={3}>
      <Box>
        <Typography component="h1" variant="h1">
          {t('admin.title')}
        </Typography>
        <Typography color="text.secondary">{t('admin.subtitle')}</Typography>
      </Box>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' } }}>
        {adminAreas.map((area) => (
          <Card key={area.to}>
            <CardActionArea component={RouterLink} to={area.to} sx={{ minHeight: 178 }}>
              <CardContent>
                <Stack spacing={2}>
                  <Box sx={{ color: 'secondary.main', display: 'flex' }}>{area.icon}</Box>
                  <Box>
                    <Typography component="h2" sx={{ fontSize: 20, fontWeight: 900 }}>
                      {t(area.titleKey)}
                    </Typography>
                    <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                      {t(area.descriptionKey)}
                    </Typography>
                  </Box>
                </Stack>
              </CardContent>
            </CardActionArea>
          </Card>
        ))}
      </Box>
    </Stack>
  );
}
