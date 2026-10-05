package com.syscrafters.salestorm.dto;

import com.syscrafters.salestorm.entity.Order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTOs {

    public static class OrderResponse {
        private Long orderId;
        private String orderNumber;
        private Long customerId;
        private Long checkoutSessionId;
        private Long paymentId;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private List<OrderItemResponse> items;
        private List<HistoryResponse> statusHistory;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

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

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public Long getCheckoutSessionId() {
            return checkoutSessionId;
        }

        public void setCheckoutSessionId(Long checkoutSessionId) {
            this.checkoutSessionId = checkoutSessionId;
        }

        public Long getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
        }

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public List<OrderItemResponse> getItems() {
            return items;
        }

        public void setItems(List<OrderItemResponse> items) {
            this.items = items;
        }

        public List<HistoryResponse> getStatusHistory() {
            return statusHistory;
        }

        public void setStatusHistory(List<HistoryResponse> statusHistory) {
            this.statusHistory = statusHistory;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public static class HistoryResponse {
        private String fromStatus;
        private String toStatus;
        private String reason;
        private LocalDateTime changedAt;

        public HistoryResponse() {}

        public HistoryResponse(String fromStatus, String toStatus, String reason, LocalDateTime changedAt) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
            this.reason = reason;
            this.changedAt = changedAt;
        }

        public String getFromStatus() {
            return fromStatus;
        }

        public void setFromStatus(String fromStatus) {
            this.fromStatus = fromStatus;
        }

        public String getToStatus() {
            return toStatus;
        }

        public void setToStatus(String toStatus) {
            this.toStatus = toStatus;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public LocalDateTime getChangedAt() {
            return changedAt;
        }

        public void setChangedAt(LocalDateTime changedAt) {
            this.changedAt = changedAt;
        }
    }

    public static class StatusUpdateRequest {
        @NotNull(message = "New target status is required")
        private OrderStatus targetStatus;
        private String reason;

        public OrderStatus getTargetStatus() {
            return targetStatus;
        }

        public void setTargetStatus(OrderStatus targetStatus) {
            this.targetStatus = targetStatus;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
