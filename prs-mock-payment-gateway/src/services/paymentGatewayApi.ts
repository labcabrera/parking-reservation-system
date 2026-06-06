import type { PaymentAttemptRequest, PaymentAttemptResponse, PaymentSession } from '../types/payment'

const PAYMENT_SERVICE_URL = (import.meta.env.VITE_PAYMENT_SERVICE_URL as string | undefined)?.replace(/\/$/, '') ?? ''
const PAYMENT_ATTEMPTS_URL = `${PAYMENT_SERVICE_URL}/payment-api/api/v1/payment-attempts`

export function readPaymentSession(search: string): PaymentSession {
  const params = new URLSearchParams(search)
  const amount = params.get('amount')

  return {
    amount: amount == null || Number.isNaN(Number(amount)) ? undefined : Number(amount),
    attemptId: params.get('attemptId') ?? undefined,
    currency: params.get('currency') ?? undefined,
    orderId: params.get('orderId') ?? undefined,
  }
}

export async function submitPaymentAttempt(request: PaymentAttemptRequest): Promise<PaymentAttemptResponse> {
  const response = await fetch(PAYMENT_ATTEMPTS_URL, {
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  })

  if (!response.ok) {
    throw new Error(`Payment gateway rejected the attempt: ${response.status} ${response.statusText}`)
  }

  return response.json() as Promise<PaymentAttemptResponse>
}
