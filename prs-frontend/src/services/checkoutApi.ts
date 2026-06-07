import type { PageResponse } from "../types/catalog";
import type {
  Checkout,
  PaymentAttemptResult,
  PaymentMethod,
  SelectOptionRequest,
} from "../types/checkout";
import { authenticatedFetch, getBaseUrl, parseJsonResponse } from "./http";

const BFF_URL = getBaseUrl("VITE_BFF_URL");
const CHECKOUT_URL = `${BFF_URL}/api/v1/checkout`;
const PAYMENT_METHODS_URL = `${BFF_URL}/api/v1/payment-methods`;

export async function selectCheckoutOption(
  request: SelectOptionRequest,
): Promise<Checkout> {
  const response = await authenticatedFetch(
    `${CHECKOUT_URL}/select-option`,
    {
      body: JSON.stringify(request),
      headers: {
        "Content-Type": "application/json",
      },
      method: "POST",
    },
  );

  return parseJsonResponse<Checkout>(
    response,
    "Checkout option could not be selected",
  );
}

export async function confirmCheckout(checkoutId: string): Promise<Checkout> {
  const response = await authenticatedFetch(
    `${CHECKOUT_URL}/${encodeURIComponent(checkoutId)}/confirm`,
    {
      method: "POST",
    },
  );

  return parseJsonResponse<Checkout>(
    response,
    "Checkout could not be confirmed",
  );
}

export async function getCheckout(checkoutId: string): Promise<Checkout> {
  const response = await authenticatedFetch(
    `${CHECKOUT_URL}/${encodeURIComponent(checkoutId)}`,
  );

  return parseJsonResponse<Checkout>(response, "Checkout could not be loaded");
}

export async function listPaymentMethods(): Promise<PaymentMethod[]> {
  const searchParams = new URLSearchParams({
    activeOnly: "true",
    page: "0",
    size: "20",
  });
  const response = await authenticatedFetch(
    `${PAYMENT_METHODS_URL}?${searchParams.toString()}`,
  );
  const page = await parseJsonResponse<PageResponse<PaymentMethod>>(
    response,
    "Payment methods could not be loaded",
  );

  return [...page.content].sort(
    (left, right) => (left.displayOrder ?? 0) - (right.displayOrder ?? 0),
  );
}

export interface ListUserReservationsRequest {
  end: string;
  facilityId?: string;
  page?: number;
  size?: number;
  sort?: string[];
  start: string;
}

export async function listUserReservations(
  request: ListUserReservationsRequest,
): Promise<Checkout[]> {
  const searchParams = new URLSearchParams({
    end: request.end,
    page: String(request.page ?? 0),
    size: String(request.size ?? 20),
    start: request.start,
  });

  if (request.facilityId) {
    searchParams.set("facilityId", request.facilityId);
  }

  request.sort?.forEach((sort) => searchParams.append("sort", sort));

  const response = await authenticatedFetch(
    `${CHECKOUT_URL}/reservations?${searchParams.toString()}`,
  );

  return parseJsonResponse<Checkout[]>(
    response,
    "User reservations could not be loaded",
  );
}

export async function initiateCheckoutPayment(
  checkoutId: string,
  paymentMethodCode: string,
  idempotencyKey: string,
): Promise<PaymentAttemptResult> {
  const response = await authenticatedFetch(
    `${CHECKOUT_URL}/${encodeURIComponent(checkoutId)}/payment-attempts`,
    {
      body: JSON.stringify({ paymentMethodCode, idempotencyKey }),
      headers: {
        "Content-Type": "application/json",
      },
      method: "POST",
    },
  );

  return parseJsonResponse<PaymentAttemptResult>(
    response,
    "Payment attempt could not be initiated",
  );
}
