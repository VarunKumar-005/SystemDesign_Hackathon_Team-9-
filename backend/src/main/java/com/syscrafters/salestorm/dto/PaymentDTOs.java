package com.syscrafters.salestorm.dto;

import com.syscrafters.salestorm.entity.Payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTOs {

    public static class PaymentProcessRequest {
        @NotNull(message = "Checkout Session ID is required")
        private Long checkoutSessionId;

        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotBlank(message = "Idempotency key is required")
        private String idempotencyKey;

        private String paymentMethod = "MOCK_CARD";

        /**
         * Scenario simulator: SUCCESS, FAILURE, TIMEOUT
         */
        private String scenario = "SUCCESS";

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

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public void setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getScenario() {
            return scenario;
        }

        public void setScenario(String scenario) {
            this.scenario = scenario;
        }
    }

    public static class PaymentResponse {
        private Long paymentId;
        private Long checkoutSessionId;
        private Long customerId;
        private Long reservationId;
        private String idempotencyKey;
        private String transactionReference;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String failureReason;
        private boolean isDuplicateReplay;
        private Long orderId;
        private String orderNumber;
        private LocalDateTime createdAt;

        public Long getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
        }

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

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public void setIdempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
        }

        public String getTransactionReference() {
            return transactionReference;
        }

        public void setTransactionReference(String transactionReference) {
            this.transactionReference = transactionReference;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public PaymentStatus getStatus() {
            return status;
        }

        public void setStatus(PaymentStatus status) {
            this.status = status;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public void setFailureReason(String failureReason) {
            this.failureReason = failureReason;
        }

        public boolean isDuplicateReplay() {
            return isDuplicateReplay;
        }

        public void setDuplicateReplay(boolean duplicateReplay) {
            isDuplicateReplay = duplicateReplay;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ReconciliationRequest {
        @NotBlank(message = "Transaction reference or idempotency key is required")
        private String reference;

        private String targetOutcome = "SUCCEEDED"; // or FAILED

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getTargetOutcome() {
            return targetOutcome;
        }

        public void setTargetOutcome(String targetOutcome) {
            this.targetOutcome = targetOutcome;
        }
    }
}
