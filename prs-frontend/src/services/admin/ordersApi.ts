import type { Order } from '../../types/order';
import { getBaseUrl, parseJsonResponse } from '../http';

const BASE_URL = `${getBaseUrl('VITE_ECOMMERCE_SERVICE_URL', '/admin-api')}/api/v1/orders`;

export async function getOrder(orderId: string): Promise<Order> {
  const response = await fetch(`${BASE_URL}/${encodeURIComponent(orderId)}`);
  return parseJsonResponse<Order>(response, 'Order could not be loaded');
}
