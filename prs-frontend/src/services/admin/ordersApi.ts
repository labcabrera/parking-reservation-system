import type { Order } from '../../types/order';
import type { PageResponse } from '../../types/catalog';
import { getBaseUrl, parseJsonResponse } from '../http';

const BASE_URL = `${getBaseUrl('VITE_ECOMMERCE_SERVICE_URL', '/admin-api')}/api/v1/orders`;

export async function listOrders(page = 0, size = 20): Promise<PageResponse<Order>> {
  const searchParams = new URLSearchParams({ page: String(page), size: String(size) });
  const response = await fetch(`${BASE_URL}?${searchParams.toString()}`);
  return parseJsonResponse<PageResponse<Order>>(response, 'Orders could not be loaded');
}

export async function getOrder(orderId: string): Promise<Order> {
  const response = await fetch(`${BASE_URL}/${encodeURIComponent(orderId)}`);
  return parseJsonResponse<Order>(response, 'Order could not be loaded');
}
