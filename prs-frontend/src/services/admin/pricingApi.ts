import type { PageResponse } from '../../types/catalog';
import type { CreatePricingRuleRequest, PricingRule, SpringPageResponse } from '../../types/pricing';
import { toPageResponse } from '../../types/pricing';
import { getBaseUrl, parseJsonResponse } from '../http';

const BASE_URL = `${getBaseUrl('VITE_PRICING_SERVICE_URL', '/admin-api')}/api/v1/pricing-rules`;

export async function listPricingRules(page = 0, size = 20): Promise<PageResponse<PricingRule>> {
  const searchParams = new URLSearchParams({ page: String(page), size: String(size) });
  const response = await fetch(`${BASE_URL}?${searchParams.toString()}`);
  const payload = await parseJsonResponse<SpringPageResponse<PricingRule>>(
    response,
    'Pricing rules could not be loaded',
  );

  return toPageResponse(payload);
}

export async function createPricingRule(request: CreatePricingRuleRequest): Promise<PricingRule> {
  const response = await fetch(BASE_URL, {
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  return parseJsonResponse<PricingRule>(response, 'Pricing rule could not be created');
}
