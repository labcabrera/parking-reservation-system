import type { Money } from './catalog';

export const BillingType = {
  DAILY: 'DAILY',
  HOURLY: 'HOURLY',
  HYBRID: 'HYBRID',
} as const;

export type BillingType = (typeof BillingType)[keyof typeof BillingType];

export const PricingRuleStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;

export type PricingRuleStatus = (typeof PricingRuleStatus)[keyof typeof PricingRuleStatus];

export interface PricingRule {
  id: string;
  name: string;
  billingType: BillingType;
  baseRate: Money;
  hourlyRate?: Money | null;
  dailyRate?: Money | null;
  taxRate?: number | null;
  status?: PricingRuleStatus | null;
  validFrom?: string | null;
  validTo?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  version?: number | null;
}

export type CreatePricingRuleRequest = Omit<PricingRule, 'createdAt' | 'id' | 'updatedAt' | 'version'>;
