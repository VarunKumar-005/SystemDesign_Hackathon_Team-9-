package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.OrderDTOs.*;
import com.syscrafters.salestorm.entity.*;
import com.syscrafters.salestorm.entity.Order.OrderStatus;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.exception.InvalidOrderStateTransitionException;
import com.syscrafters.salestorm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final ProductRepository productRepository;
    private final OutboxService outboxService;

    // Strict state transition graph
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),
            OrderStatus.PAYMENT_PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.DELIVERED, Collections.emptySet(), // Terminal state
            OrderStatus.CANCELLED, Collections.emptySet()  // Terminal state
    );

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        CheckoutSessionRepository checkoutSessionRepository,
                        ProductRepository productRepository,
                        OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.productRepository = productRepository;
        this.outboxService = outboxService;
    }

    /**
     * Create and confirm order after successful payment.
     * Idempotent check ensures duplicate payment events don't recreate the order.
     */
    @Transactional
    public OrderResponse createOrderFromPayment(Long checkoutSessionId, Long paymentId) {
        Optional<Order> existing = orderRepository.findByPaymentId(paymentId);
        if (existing.isPresent()) {
            log.info("Order already exists for payment [{}]: order [{}]", paymentId, existing.get().getOrderNumber());
            return toResponse(existing.get());
        }

        CheckoutSession session = checkoutSessionRepository.findById(checkoutSessionId)
                .orElseThrow(() -> new BusinessException("Checkout session not found: " + checkoutSessionId));

        Product product = productRepository.findById(session.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found: " + session.getProductId()));

        String orderNumber = "ORD-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Order order = new Order(
                orderNumber,
                session.getCustomerId(),
                session.getId(),
                paymentId,
                session.getTotalAmount(),
                "Customer Delivery Address"
        );
        order.setStatus(OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        OrderItem item = new OrderItem(product.getId(), product.getName(), session.getQuantity(), session.getUnitPrice());
        order.addItem(item);
        orderItemRepository.save(item);

        statusHistoryRepository.save(new OrderStatusHistory(order.getId(), null, OrderStatus.CREATED.name(), "Order created"));
        statusHistoryRepository.save(new OrderStatusHistory(order.getId(), OrderStatus.CREATED.name(), OrderStatus.CONFIRMED.name(), "Payment confirmed via Kafka event"));

        outboxService.publishEvent(
                "Order",
                order.getId().toString(),
                "OrderConfirmed",
                Map.of("orderId", order.getId(), "orderNumber", order.getOrderNumber())
        );

        log.info("Created and confirmed Order [{}] for payment [{}]", order.getOrderNumber(), paymentId);
        return toResponse(order);
    }

    /**
     * Advance / update order status with strict state machine validation.
     * Only the Order Service should update order status.
     * Rejects invalid transitions like DELIVERED -> PAYMENT_PENDING.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus targetStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == targetStatus) {
            return toResponse(order);
        }

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(targetStatus)) {
            log.warn("Illegal order state transition attempt: {} -> {}", currentStatus, targetStatus);
            throw new InvalidOrderStateTransitionException(currentStatus.name(), targetStatus.name());
        }

        order.setStatus(targetStatus);
        order = orderRepository.save(order);

        statusHistoryRepository.save(new OrderStatusHistory(
                order.getId(),
                currentStatus.name(),
                targetStatus.name(),
                reason != null ? reason : "Status transitioned to " + targetStatus
        ));

        // Publish event for order status change
        String eventType = switch (targetStatus) {
            case PROCESSING -> "OrderProcessing";
            case SHIPPED -> "OrderShipped";
            case OUT_FOR_DELIVERY -> "OrderOutForDelivery";
            case DELIVERED -> "OrderDelivered";
            case CANCELLED -> "OrderCancelled";
            default -> "OrderStatusUpdated";
        };

        outboxService.publishEvent(
                "Order",
                order.getId().toString(),
                eventType,
                Map.of("orderId", order.getId(), "status", targetStatus.name())
        );

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED, reason != null ? reason : "Customer cancelled order");
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found: " + orderId));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setOrderId(order.getId());
        resp.setOrderNumber(order.getOrderNumber());
        resp.setCustomerId(order.getCustomerId());
        resp.setCheckoutSessionId(order.getCheckoutSessionId());
        resp.setPaymentId(order.getPaymentId());
        resp.setStatus(order.getStatus());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setShippingAddress(order.getShippingAddress());
        resp.setCreatedAt(order.getCreatedAt());
        resp.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(it -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setId(it.getId());
            r.setProductId(it.getProductId());
            r.setProductName(it.getProductName());
            r.setQuantity(it.getQuantity());
            r.setUnitPrice(it.getUnitPrice());
            r.setTotalPrice(it.getTotalPrice());
            return r;
        }).collect(Collectors.toList());
        resp.setItems(itemResponses);

        List<HistoryResponse> historyResponses = statusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId())
                .stream().map(h -> new HistoryResponse(h.getFromStatus(), h.getToStatus(), h.getReason(), h.getChangedAt()))
                .collect(Collectors.toList());
        resp.setStatusHistory(historyResponses);

        return resp;
    }
}
