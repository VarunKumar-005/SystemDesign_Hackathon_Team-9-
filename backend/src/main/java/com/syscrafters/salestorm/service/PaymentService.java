package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.PaymentDTOs.PaymentProcessRequest;
import com.syscrafters.salestorm.dto.PaymentDTOs.PaymentResponse;
import com.syscrafters.salestorm.dto.PaymentDTOs.ReconciliationRequest;
import com.syscrafters.salestorm.entity.*;
import com.syscrafters.salestorm.entity.CheckoutSession.CheckoutStatus;
import com.syscrafters.salestorm.entity.InventoryReservation.ReservationStatus;
import com.syscrafters.salestorm.entity.Payment.PaymentStatus;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final InventoryReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OutboxService outboxService;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final OrderService orderService;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentAttemptRepository paymentAttemptRepository,
                          CheckoutSessionRepository checkoutSessionRepository,
                          InventoryReservationRepository reservationRepository,
                          OrderRepository orderRepository,
                          InventoryService inventoryService,
                          OutboxService outboxService,
                          PaymentGatewayFactory paymentGatewayFactory,
                          @Lazy OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.outboxService = outboxService;
        this.paymentGatewayFactory = paymentGatewayFactory;
        this.orderService = orderService;
    }

    /**
     * IDEMPOTENT PAYMENT PROCESSING
     * 1. Checks if customer already executed a payment with this idempotency key.
     * 2. If already exists -> returns the existing payment record immediately without charging again.
     * 3. Prevents one reservation from having multiple successful payments.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentProcessRequest request) {
        // 1. Idempotency Check
        Optional<Payment> existingPayment = paymentRepository
                .findByCustomerIdAndIdempotencyKey(request.getCustomerId(), request.getIdempotencyKey());

        if (existingPayment.isPresent()) {
            Payment p = existingPayment.get();
            log.info("Idempotent replay detected for payment key [{}] (customer [{}])", request.getIdempotencyKey(), request.getCustomerId());
            PaymentResponse resp = toResponse(p);
            resp.setDuplicateReplay(true);
            return resp;
        }

        // 2. Validate Checkout Session & Reservation
        CheckoutSession session = checkoutSessionRepository.findById(request.getCheckoutSessionId())
                .orElseThrow(() -> new BusinessException("Checkout session not found: " + request.getCheckoutSessionId()));

        if (!session.getCustomerId().equals(request.getCustomerId())) {
            throw new BusinessException("Checkout session does not belong to customer: " + request.getCustomerId());
        }

        InventoryReservation reservation = reservationRepository.findById(session.getReservationId())
                .orElseThrow(() -> new BusinessException("Reservation not found: " + session.getReservationId()));

        // Rule #4: One reservation cannot create multiple successful payments
        Optional<Payment> existingForRes = paymentRepository.findByReservationId(reservation.getId());
        if (existingForRes.isPresent() && existingForRes.get().getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BusinessException("A successful payment already exists for this reservation.");
        }

        // Validate expiration
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
            inventoryService.releaseStock(reservation.getProductId(), reservation.getQuantity());
            throw new BusinessException("Reservation has expired. Inventory was released.");
        }

        // 3. Create INITIATED Payment Record in Database
        String transactionRef = "TX-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        Payment payment = new Payment(
                request.getCustomerId(),
                session.getId(),
                reservation.getId(),
                request.getIdempotencyKey(),
                transactionRef,
                session.getTotalAmount(),
                "INR",
                request.getPaymentMethod()
        );

        try {
            payment = paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException ex) {
            // Concurrent race on same idempotency key
            log.warn("Database unique constraint caught duplicate payment attempt: {}", ex.getMessage());
            Payment conflict = paymentRepository.findByCustomerIdAndIdempotencyKey(request.getCustomerId(), request.getIdempotencyKey())
                    .orElseThrow(() -> new BusinessException("Concurrent payment request conflict"));
            PaymentResponse resp = toResponse(conflict);
            resp.setDuplicateReplay(true);
            return resp;
        }

        // Record PaymentInitiated event
        outboxService.publishEvent(
                "Payment",
                payment.getId().toString(),
                "PaymentInitiated",
                Map.of(
                        "paymentId", payment.getId(),
                        "transactionRef", transactionRef,
                        "amount", payment.getAmount()
                )
        );

        // 4. Call Payment Gateway Strategy
        PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway(request.getPaymentMethod());
        PaymentGateway.PaymentGatewayResult gatewayResult = gateway.processPayment(
                transactionRef,
                payment.getAmount(),
                payment.getCurrency(),
                request.getScenario()
        );

        // Log payment attempt
        paymentAttemptRepository.save(new PaymentAttempt(
                payment.getId(),
                1,
                gatewayResult.status().name(),
                gatewayResult.message()
        ));

        // 5. Update Status based on Gateway Result
        payment.setStatus(gatewayResult.status());
        payment.setFailureReason(gatewayResult.message());
        payment = paymentRepository.save(payment);

        if (gatewayResult.status() == PaymentStatus.SUCCEEDED) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setConfirmedAt(LocalDateTime.now());
            reservationRepository.save(reservation);

            session.setStatus(CheckoutStatus.COMPLETED);
            checkoutSessionRepository.save(session);

            // Move stock to sold atomically
            inventoryService.confirmSold(reservation.getProductId(), reservation.getQuantity());

            // Place PaymentSucceeded event into Outbox table
            outboxService.publishEvent(
                    "Payment",
                    payment.getId().toString(),
                    "PaymentSucceeded",
                    Map.of(
                            "paymentId", payment.getId(),
                            "checkoutSessionId", session.getId(),
                            "customerId", payment.getCustomerId(),
                            "amount", payment.getAmount()
                    )
            );
        } else if (gatewayResult.status() == PaymentStatus.FAILED) {
            reservation.setStatus(ReservationStatus.PAYMENT_FAILED);
            reservationRepository.save(reservation);

            // Release stock back to inventory atomically
            inventoryService.releaseStock(reservation.getProductId(), reservation.getQuantity());

            outboxService.publishEvent(
                    "Payment",
                    payment.getId().toString(),
                    "PaymentFailed",
                    Map.of(
                            "paymentId", payment.getId(),
                            "reason", gatewayResult.message()
                    )
            );
        } else if (gatewayResult.status() == PaymentStatus.UNKNOWN) {
            // Rule #5: Payment timeout must NOT automatically mean payment failure!
            // Reservation remains temporarily reserved
            log.warn("Payment status is UNKNOWN. Stock remains reserved pending reconciliation.");
        }

        return toResponse(payment);
    }

    /**
     * RECONCILIATION FOR TIMED-OUT / UNKNOWN PAYMENTS
     */
    @Transactional
    public PaymentResponse reconcilePayment(ReconciliationRequest request) {
        Payment payment = paymentRepository.findByTransactionReference(request.getReference())
                .or(() -> paymentRepository.findByIdempotencyKey(request.getReference()))
                .orElseThrow(() -> new BusinessException("Payment record not found for reference: " + request.getReference()));

        if (payment.getStatus() != PaymentStatus.UNKNOWN && payment.getStatus() != PaymentStatus.PENDING) {
            log.info("Payment [{}] already resolved to [{}], no reconciliation needed", payment.getId(), payment.getStatus());
            return toResponse(payment);
        }

        CheckoutSession session = checkoutSessionRepository.findById(payment.getCheckoutSessionId())
                .orElseThrow(() -> new BusinessException("Checkout session not found"));
        InventoryReservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new BusinessException("Reservation not found"));

        if ("SUCCEEDED".equalsIgnoreCase(request.getTargetOutcome())) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setFailureReason("Reconciled as SUCCEEDED via payment gateway confirmation");
            paymentRepository.save(payment);

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);

            session.setStatus(CheckoutStatus.COMPLETED);
            checkoutSessionRepository.save(session);

            inventoryService.confirmSold(reservation.getProductId(), reservation.getQuantity());

            outboxService.publishEvent(
                    "Payment",
                    payment.getId().toString(),
                    "PaymentSucceeded",
                    Map.of(
                            "paymentId", payment.getId(),
                            "checkoutSessionId", session.getId(),
                            "customerId", payment.getCustomerId(),
                            "amount", payment.getAmount()
                    )
            );
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Reconciled as FAILED: Payment authorization expired or rejected by provider");
            paymentRepository.save(payment);

            reservation.setStatus(ReservationStatus.PAYMENT_FAILED);
            reservationRepository.save(reservation);

            inventoryService.releaseStock(reservation.getProductId(), reservation.getQuantity());

            outboxService.publishEvent(
                    "Payment",
                    payment.getId().toString(),
                    "PaymentFailed",
                    Map.of("paymentId", payment.getId(), "reason", "Reconciled as FAILED")
            );
        }

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentId(p.getId());
        resp.setCheckoutSessionId(p.getCheckoutSessionId());
        resp.setCustomerId(p.getCustomerId());
        resp.setReservationId(p.getReservationId());
        resp.setIdempotencyKey(p.getIdempotencyKey());
        resp.setTransactionReference(p.getTransactionReference());
        resp.setAmount(p.getAmount());
        resp.setCurrency(p.getCurrency());
        resp.setStatus(p.getStatus());
        resp.setFailureReason(p.getFailureReason());
        resp.setCreatedAt(p.getCreatedAt());

        orderRepository.findByPaymentId(p.getId()).ifPresent(order -> {
            resp.setOrderId(order.getId());
            resp.setOrderNumber(order.getOrderNumber());
        });

        return resp;
    }
}
