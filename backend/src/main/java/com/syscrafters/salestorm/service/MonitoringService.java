package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.MonitoringMetricsDTO;
import com.syscrafters.salestorm.dto.MonitoringMetricsDTO.*;
import com.syscrafters.salestorm.entity.*;
import com.syscrafters.salestorm.entity.InventoryReservation.ReservationStatus;
import com.syscrafters.salestorm.entity.Order.OrderStatus;
import com.syscrafters.salestorm.entity.Payment.PaymentStatus;
import com.syscrafters.salestorm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MonitoringService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final EventConsumerService eventConsumerService;

    private final AtomicLong apiRequestCounter = new AtomicLong(0);
    private final AtomicLong failedRequestCounter = new AtomicLong(0);

    public MonitoringService(ProductRepository productRepository,
                             InventoryRepository inventoryRepository,
                             InventoryReservationRepository reservationRepository,
                             PaymentRepository paymentRepository,
                             OrderRepository orderRepository,
                             OutboxService outboxService,
                             EventConsumerService eventConsumerService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
        this.eventConsumerService = eventConsumerService;
    }

    public void incrementApiRequests() {
        apiRequestCounter.incrementAndGet();
    }

    public void incrementFailedRequests() {
        failedRequestCounter.incrementAndGet();
    }

    @Transactional(readOnly = true)
    public MonitoringMetricsDTO getMetrics() {
        MonitoringMetricsDTO metrics = new MonitoringMetricsDTO();

        // 1. Inventory Metrics
        InventoryMetrics invMetrics = new InventoryMetrics();
        productRepository.findById(1L).ifPresentOrElse(prod -> {
            invMetrics.setProductId(prod.getId());
            invMetrics.setProductName(prod.getName());
            inventoryRepository.findByProductId(prod.getId()).ifPresent(inv -> {
                invMetrics.setTotalStock(inv.getTotalQuantity());
                invMetrics.setAvailableStock(inv.getAvailableQuantity());
                invMetrics.setReservedStock(inv.getReservedQuantity());
                invMetrics.setSoldStock(inv.getSoldQuantity());
            });
        }, () -> {
            invMetrics.setProductName("No Product");
        });
        metrics.setInventory(invMetrics);

        // 2. Reservation Metrics
        ReservationMetrics resMetrics = new ReservationMetrics();
        resMetrics.setTotal(reservationRepository.count());
        resMetrics.setActive(reservationRepository.findActiveReservations(LocalDateTime.now()).size());
        resMetrics.setExpired(reservationRepository.countByStatus(ReservationStatus.EXPIRED));
        resMetrics.setReleased(reservationRepository.countByStatus(ReservationStatus.RELEASED));
        resMetrics.setConfirmed(reservationRepository.countByStatus(ReservationStatus.CONFIRMED));
        resMetrics.setFailed(reservationRepository.countByStatus(ReservationStatus.PAYMENT_FAILED));
        metrics.setReservations(resMetrics);

        // 3. Payment Metrics
        PaymentMetrics payMetrics = new PaymentMetrics();
        payMetrics.setSuccessful(paymentRepository.countByStatus(PaymentStatus.SUCCEEDED));
        payMetrics.setFailed(paymentRepository.countByStatus(PaymentStatus.FAILED));
        payMetrics.setPending(paymentRepository.countByStatus(PaymentStatus.PENDING));
        payMetrics.setUnknown(paymentRepository.countByStatus(PaymentStatus.UNKNOWN));
        payMetrics.setRefunded(paymentRepository.countByStatus(PaymentStatus.REFUNDED));
        payMetrics.setInitiated(paymentRepository.countByStatus(PaymentStatus.INITIATED));
        metrics.setPayments(payMetrics);

        // 4. Order Metrics
        OrderMetrics ordMetrics = new OrderMetrics();
        ordMetrics.setTotal(orderRepository.count());
        ordMetrics.setCreated(orderRepository.countByStatus(OrderStatus.CREATED));
        ordMetrics.setPaymentPending(orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING));
        ordMetrics.setConfirmed(orderRepository.countByStatus(OrderStatus.CONFIRMED));
        ordMetrics.setProcessing(orderRepository.countByStatus(OrderStatus.PROCESSING));
        ordMetrics.setShipped(orderRepository.countByStatus(OrderStatus.SHIPPED));
        ordMetrics.setOutForDelivery(orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY));
        ordMetrics.setDelivered(orderRepository.countByStatus(OrderStatus.DELIVERED));
        ordMetrics.setCancelled(orderRepository.countByStatus(OrderStatus.CANCELLED));
        metrics.setOrders(ordMetrics);

        // 5. System Metrics
        SystemMetrics sysMetrics = new SystemMetrics();
        sysMetrics.setApiRequests(apiRequestCounter.get());
        sysMetrics.setFailedRequests(failedRequestCounter.get());
        sysMetrics.setKafkaEvents(outboxService.getTotalCount());
        sysMetrics.setPendingEvents(outboxService.getPendingCount());
        sysMetrics.setFailedEvents(outboxService.getFailedCount());
        sysMetrics.setProcessedEvents(eventConsumerService.getProcessedCount());
        metrics.setSystem(sysMetrics);

        metrics.setOrderServiceAvailable(eventConsumerService.isOrderServiceAvailable());

        return metrics;
    }
}
