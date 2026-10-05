package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.ReservationDTOs.ReservationRequest;
import com.syscrafters.salestorm.dto.ReservationDTOs.ReservationResponse;
import com.syscrafters.salestorm.entity.InventoryReservation;
import com.syscrafters.salestorm.entity.InventoryReservation.ReservationStatus;
import com.syscrafters.salestorm.entity.Product;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.exception.InsufficientStockException;
import com.syscrafters.salestorm.repository.InventoryReservationRepository;
import com.syscrafters.salestorm.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final InventoryReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final OutboxService outboxService;

    @Value("${salestorm.reservation.expiry-seconds:300}")
    private int defaultExpirySeconds;

    public ReservationService(InventoryReservationRepository reservationRepository,
                              InventoryService inventoryService,
                              ProductRepository productRepository,
                              OutboxService outboxService) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.productRepository = productRepository;
        this.outboxService = outboxService;
    }

    /**
     * Create an inventory reservation atomically.
     * Uses atomic SQL update in PostgreSQL to prevent overselling.
     */
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found: " + request.getProductId()));

        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = "RES-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Check if customer already submitted with this idempotency key
        Optional<InventoryReservation> existing = reservationRepository
                .findByCustomerIdAndIdempotencyKey(request.getCustomerId(), idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent replay for reservation customer [{}], key [{}]", request.getCustomerId(), idempotencyKey);
            return toResponse(existing.get(), product.getName());
        }

        // ATOMIC DB RESERVATION: No read-then-write
        boolean reserved = inventoryService.reserveStock(request.getProductId(), request.getQuantity());
        if (!reserved) {
            throw new InsufficientStockException("Insufficient stock available for product: " + product.getName());
        }

        int durationSeconds = product.getReservationDurationSeconds() > 0
                ? product.getReservationDurationSeconds()
                : defaultExpirySeconds;

        InventoryReservation reservation = new InventoryReservation(
                request.getCustomerId(),
                request.getProductId(),
                request.getQuantity(),
                durationSeconds,
                idempotencyKey
        );

        reservation = reservationRepository.save(reservation);

        // Record ReservationCreated event into Outbox table
        outboxService.publishEvent(
                "Reservation",
                reservation.getId().toString(),
                "ReservationCreated",
                Map.of(
                        "reservationId", reservation.getId(),
                        "customerId", reservation.getCustomerId(),
                        "productId", reservation.getProductId(),
                        "quantity", reservation.getQuantity(),
                        "expiresAt", reservation.getExpiresAt().toString()
                )
        );

        return toResponse(reservation, product.getName());
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long reservationId) {
        InventoryReservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("Reservation not found: " + reservationId));
        String productName = productRepository.findById(res.getProductId())
                .map(Product::getName).orElse("Unknown Product");
        return toResponse(res, productName);
    }

    /**
     * Cancel / release a reservation explicitly.
     */
    @Transactional
    public void cancelReservation(Long reservationId) {
        InventoryReservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("Reservation not found: " + reservationId));

        if (res.getStatus() == ReservationStatus.RESERVED || res.getStatus() == ReservationStatus.PAYMENT_PENDING) {
            res.setStatus(ReservationStatus.RELEASED);
            res.setReleasedAt(LocalDateTime.now());
            reservationRepository.save(res);

            inventoryService.releaseStock(res.getProductId(), res.getQuantity());

            outboxService.publishEvent(
                    "Reservation",
                    res.getId().toString(),
                    "ReservationExpired",
                    Map.of(
                            "reservationId", res.getId(),
                            "productId", res.getProductId(),
                            "quantity", res.getQuantity()
                    )
            );
        }
    }

    /**
     * Check and process expired reservations.
     * RESERVED / PAYMENT_PENDING -> EXPIRED -> RELEASED
     * Automatically returns quantity to available inventory.
     */
    @Transactional
    public int processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<InventoryReservation> expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, now);

        int count = 0;
        for (InventoryReservation res : expiredReservations) {
            res.setStatus(ReservationStatus.RELEASED);
            res.setReleasedAt(now);
            reservationRepository.save(res);

            // Return stock to available inventory atomically
            inventoryService.releaseStock(res.getProductId(), res.getQuantity());

            outboxService.publishEvent(
                    "Reservation",
                    res.getId().toString(),
                    "ReservationExpired",
                    Map.of(
                            "reservationId", res.getId(),
                            "productId", res.getProductId(),
                            "quantity", res.getQuantity()
                    )
            );
            count++;
        }
        return count;
    }

    private ReservationResponse toResponse(InventoryReservation res, String productName) {
        ReservationResponse dto = new ReservationResponse();
        dto.setReservationId(res.getId());
        dto.setCustomerId(res.getCustomerId());
        dto.setProductId(res.getProductId());
        dto.setProductName(productName);
        dto.setQuantity(res.getQuantity());
        dto.setStatus(res.getStatus());
        dto.setIdempotencyKey(res.getIdempotencyKey());
        dto.setCreatedAt(res.getCreatedAt());
        dto.setExpiresAt(res.getExpiresAt());

        long remaining = Duration.between(LocalDateTime.now(), res.getExpiresAt()).getSeconds();
        dto.setRemainingSeconds(Math.max(0, remaining));
        return dto;
    }
}
