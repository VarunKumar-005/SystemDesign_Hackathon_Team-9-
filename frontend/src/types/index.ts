export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ProductDetailDTO {
  id: number;
  name: string;
  description: string;
  originalPrice: number;
  salePrice: number;
  imageUrl: string;
  flashSaleActive: boolean;
  reservationDurationSeconds: number;
  availableStock: number;
  totalStock: number;
  reservedStock: number;
  soldStock: number;
  saleStartTime: string;
  saleEndTime: string;
}

export interface ReservationRequest {
  productId: number;
  customerId: number;
  quantity: number;
  idempotencyKey?: string;
}

export interface ReservationResponse {
  reservationId: number;
  customerId: number;
  productId: number;
  productName: string;
  quantity: number;
  status: 'RESERVED' | 'SOLD' | 'RELEASED';
  idempotencyKey: string;
  createdAt: string;
  expiresAt: string;
  remainingSeconds: number;
}

export interface CheckoutInitRequest {
  reservationId: number;
  customerId: number;
}

export interface CheckoutResponse {
  checkoutSessionId: number;
  customerId: number;
  reservationId: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  checkoutStatus: 'PENDING' | 'COMPLETED' | 'EXPIRED' | 'CANCELLED';
  reservationStatus: 'RESERVED' | 'SOLD' | 'RELEASED';
  expiresAt: string;
  remainingSeconds: number;
}

export interface PaymentProcessRequest {
  checkoutSessionId: number;
  customerId: number;
  idempotencyKey: string;
  paymentMethod: string;
  scenario: 'SUCCESS' | 'FAILURE' | 'TIMEOUT';
}

export interface PaymentResponse {
  paymentId: number;
  checkoutSessionId: number;
  customerId: number;
  reservationId: number;
  idempotencyKey: string;
  transactionReference: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'TIMED_OUT';
  failureReason?: string;
  duplicateReplay: boolean;
  orderId?: number;
  orderNumber?: string;
  createdAt: string;
}

export interface OrderItemResponse {
  itemId: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface OrderStatusHistoryResponse {
  historyId: number;
  previousStatus: string;
  newStatus: string;
  reason: string;
  createdAt: string;
}

export interface OrderResponse {
  orderId: number;
  orderNumber: string;
  customerId: number;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
  totalAmount: number;
  currency: string;
  paymentTransactionRef?: string;
  cancelReason?: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemResponse[];
  history: OrderStatusHistoryResponse[];
}

export interface MonitoringMetricsDTO {
  inventory: {
    productName: string;
    productId: number;
    totalStock: number;
    availableStock: number;
    reservedStock: number;
    soldStock: number;
  };
  reservations: {
    total: number;
    active: number;
    expired: number;
    released: number;
    confirmed: number;
    failed: number;
  };
  payments: {
    successful: number;
    failed: number;
    pending: number;
    unknown: number;
    refunded: number;
    initiated: number;
  };
  orders: {
    created: number;
    paymentPending: number;
    confirmed: number;
    processing: number;
    shipped: number;
    outForDelivery: number;
    delivered: number;
    cancelled: number;
    total: number;
  };
  system: {
    apiRequests: number;
    failedRequests: number;
    kafkaEvents: number;
    pendingEvents: number;
    failedEvents: number;
    processedEvents: number;
  };
  orderServiceAvailable: boolean;
}

export interface LoadSimulationRequest {
  productId: number;
  availableStock: number;
  concurrentRequests: number;
  quantityPerRequest: number;
}

export interface LoadSimulationResponse {
  initialAvailableStock: number;
  totalRequests: number;
  successfulReservations: number;
  rejectedRequests: number;
  oversoldQuantity: number;
  finalAvailableStock: number;
  finalReservedStock: number;
  durationMillis: number;
  requestsPerSecond: number;
  invariantSatisfied: boolean;
  message: string;
}

export interface UserSession {
  customerId: number;
  email: string;
  fullName: string;
  role: 'CUSTOMER' | 'ADMIN';
  token?: string;
}
