package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.CheckoutDTOs.CheckoutInitRequest;
import com.syscrafters.salestorm.dto.CheckoutDTOs.CheckoutResponse;
import com.syscrafters.salestorm.entity.CheckoutSession;
import com.syscrafters.salestorm.entity.CheckoutSession.CheckoutStatus;
import com.syscrafters.salestorm.entity.InventoryReservation;
import com.syscrafters.salestorm.entity.InventoryReservation.ReservationStatus;
import com.syscrafters.salestorm.entity.Product;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.repository.CheckoutSessionRepository;
import com.syscrafters.salestorm.repository.InventoryReservationRepository;
import com.syscrafters.salestorm.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CheckoutService {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProductRepository productRepository;

    public CheckoutService(CheckoutSessionRepository checkoutSessionRepository,
                           InventoryReservationRepository reservationRepository,
                           ProductRepository productRepository) {
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.reservationRepository = reservationRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CheckoutResponse initiateCheckout(CheckoutInitRequest request) {
        InventoryReservation res = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new BusinessException("Reservation not found: " + request.getReservationId()));

        if (!res.getCustomerId().equals(request.getCustomerId())) {
            throw new BusinessException("Reservation does not belong to customer: " + request.getCustomerId());
        }

        if (res.getStatus() == ReservationStatus.EXPIRED || res.getStatus() == ReservationStatus.RELEASED) {
            throw new BusinessException("Reservation has expired or been released. Please reserve again.");
        }

        if (res.getExpiresAt().isBefore(LocalDateTime.now())) {
            res.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(res);
            throw new BusinessException("Reservation has expired. Inventory was released.");
        }

        // Advance to PAYMENT_PENDING if currently RESERVED
        if (res.getStatus() == ReservationStatus.RESERVED) {
            res.setStatus(ReservationStatus.PAYMENT_PENDING);
            reservationRepository.save(res);
        }

        Product product = productRepository.findById(res.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found: " + res.getProductId()));

        BigDecimal unitPrice = product.isFlashSaleActive() ? product.getSalePrice() : product.getOriginalPrice();
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(res.getQuantity()));

        CheckoutSession session = checkoutSessionRepository.findByReservationId(res.getId())
                .orElseGet(() -> checkoutSessionRepository.save(new CheckoutSession(
                        res.getCustomerId(),
                        res.getId(),
                        res.getProductId(),
                        res.getQuantity(),
                        unitPrice,
                        totalAmount,
                        res.getExpiresAt()
                )));

        return toResponse(session, res, product.getName());
    }

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutSession(Long sessionId) {
        CheckoutSession session = checkoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Checkout session not found: " + sessionId));

        InventoryReservation res = reservationRepository.findById(session.getReservationId())
                .orElseThrow(() -> new BusinessException("Reservation not found: " + session.getReservationId()));

        Product product = productRepository.findById(session.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found: " + session.getProductId()));

        return toResponse(session, res, product.getName());
    }

    private CheckoutResponse toResponse(CheckoutSession session, InventoryReservation res, String productName) {
        CheckoutResponse resp = new CheckoutResponse();
        resp.setCheckoutSessionId(session.getId());
        resp.setCustomerId(session.getCustomerId());
        resp.setReservationId(session.getReservationId());
        resp.setProductId(session.getProductId());
        resp.setProductName(productName);
        resp.setQuantity(session.getQuantity());
        resp.setUnitPrice(session.getUnitPrice());
        resp.setTotalAmount(session.getTotalAmount());
        resp.setCheckoutStatus(session.getStatus());
        resp.setReservationStatus(res.getStatus());
        resp.setExpiresAt(res.getExpiresAt());

        long remaining = Duration.between(LocalDateTime.now(), res.getExpiresAt()).getSeconds();
        resp.setRemainingSeconds(Math.max(0, remaining));
        return resp;
    }
}
