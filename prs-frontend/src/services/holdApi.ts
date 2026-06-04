import type { CreateHoldRequest, HoldCreatedResponse, HoldResponse } from '../types/hold';

const BASE_URL = '/api/v1/reservations/holds';

export async function createHold(request: CreateHoldRequest): Promise<HoldCreatedResponse> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(`Failed to create hold: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<HoldCreatedResponse>;
}

export async function getHold(holdId: string): Promise<HoldResponse> {
  const response = await fetch(`${BASE_URL}/${holdId}`);

  if (response.status === 404) {
    throw new Error(`Hold not found: ${holdId}`);
  }

  if (!response.ok) {
    throw new Error(`Failed to get hold: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<HoldResponse>;
}

export async function releaseHold(holdId: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/${holdId}`, { method: 'DELETE' });

  if (response.status === 409) {
    throw new Error('Hold is in a terminal state and cannot be released');
  }

  if (!response.ok && response.status !== 204) {
    throw new Error(`Failed to release hold: ${response.status} ${response.statusText}`);
  }
}
