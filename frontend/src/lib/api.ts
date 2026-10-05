import {
  ApiResponse,
  ProductDetailDTO,
  ReservationRequest,
  ReservationResponse,
  CheckoutInitRequest,
  CheckoutResponse,
  PaymentProcessRequest,
  PaymentResponse,
  OrderResponse,
  MonitoringMetricsDTO,
  LoadSimulationRequest,
  LoadSimulationResponse,
} from '@/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export function generateIdempotencyKey(prefix = 'req'): string {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...(options.headers || {}),
  };

  try {
    const res = await fetch(url, { ...options, headers });
    const json: ApiResponse<T> = await res.json();
    if (!res.ok || !json.success) {
      throw new Error(json.message || `Request failed with status ${res.status}`);
    }
    return json.data;
  } catch (err: unknown) {
    if (err instanceof Error) {
      throw err;
    }
    throw new Error('An unexpected network error occurred');
  }
}

export const api = {
  // Products
  getProducts: () => request<ProductDetailDTO[]>('/products'),
  getFlashSaleProducts: () => request<ProductDetailDTO[]>('/products/flash-sale'),
  getProductById: (id: number) => request<ProductDetailDTO>(`/products/${id}`),

  // Reservations
  createReservation: (data: ReservationRequest) =>
    request<ReservationResponse>('/reservations', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getReservation: (id: number) => request<ReservationResponse>(`/reservations/${id}`),
  cancelReservation: (id: number) =>
    request<string>(`/reservations/${id}`, {
      method: 'DELETE',
    }),
  triggerExpiryCheck: () =>
    request<number>('/reservations/expire-check', {
      method: 'POST',
    }),

  // Checkout
  createCheckoutSession: (data: CheckoutInitRequest) =>
    request<CheckoutResponse>('/checkouts', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getCheckoutSession: (id: number) => request<CheckoutResponse>(`/checkouts/${id}`),
  processPayment: (checkoutId: number, data: PaymentProcessRequest) =>
    request<PaymentResponse>(`/checkouts/${checkoutId}/payments`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  // Orders
  getOrders: (customerId?: number) =>
    request<OrderResponse[]>(customerId ? `/orders?customerId=${customerId}` : '/orders'),
  getOrderById: (id: number) => request<OrderResponse>(`/orders/${id}`),
  cancelOrder: (id: number, reason = 'Customer requested cancellation') =>
    request<OrderResponse>(`/orders/${id}/cancel?reason=${encodeURIComponent(reason)}`, {
      method: 'POST',
    }),
  updateOrderStatus: (id: number, targetStatus: string, reason = 'Admin simulation update') =>
    request<OrderResponse>(`/orders/${id}/status`, {
      method: 'POST',
      body: JSON.stringify({ targetStatus, reason }),
    }),

  // Admin & Monitoring
  getMetrics: () => request<MonitoringMetricsDTO>('/admin/metrics'),
  simulateLoad: (data: LoadSimulationRequest) =>
    request<LoadSimulationResponse>('/admin/simulate-load', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  toggleOrderService: (available?: boolean) => {
    const query = available !== undefined ? `?available=${available}` : '';
    return request<{ orderServiceAvailable: boolean; statusDescription: string }>(
      `/admin/order-service-toggle${query}`,
      { method: 'POST' }
    );
  },
  resetInventory: (productId = 1, stock = 100) =>
    request<string>(`/admin/reset?productId=${productId}&stock=${stock}`, {
      method: 'POST',
    }),

  // Auth
  login: (email: string, password: string) =>
    request<{ token: string; customerId: number; email: string; fullName: string; role: 'CUSTOMER' | 'ADMIN' }>(
      '/auth/login',
      {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      }
    ),
};
