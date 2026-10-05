package com.syscrafters.salestorm.dto;

import com.syscrafters.salestorm.entity.CheckoutSession.CheckoutStatus;
import com.syscrafters.salestorm.entity.InventoryReservation.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckoutDTOs {

    public static class CheckoutInitRequest {
        @NotNull(message = "Reservation ID is required")
        private Long reservationId;

        @NotNull(message = "Customer ID is required")
        private Long customerId;

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
    }

    public static class CheckoutResponse {
        private Long checkoutSessionId;
        private Long customerId;
        private Long reservationId;
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private CheckoutStatus checkoutStatus;
        private ReservationStatus reservationStatus;
        private LocalDateTime expiresAt;
        private long remainingSeconds;

        public Long getCheckoutSessionId() {
            return checkoutSessionId;
        }

        public void setCheckoutSessionId(Long checkoutSessionId) {
            this.checkoutSessionId = checkoutSessionId;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public CheckoutStatus getCheckoutStatus() {
            return checkoutStatus;
        }

        public void setCheckoutStatus(CheckoutStatus checkoutStatus) {
            this.checkoutStatus = checkoutStatus;
        }

        public ReservationStatus getReservationStatus() {
            return reservationStatus;
        }

        public void setReservationStatus(ReservationStatus reservationStatus) {
            this.reservationStatus = reservationStatus;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
        }

        public long getRemainingSeconds() {
            return remainingSeconds;
        }

        public void setRemainingSeconds(long remainingSeconds) {
            this.remainingSeconds = remainingSeconds;
        }
    }
}
