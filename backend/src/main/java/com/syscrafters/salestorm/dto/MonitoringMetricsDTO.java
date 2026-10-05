package com.syscrafters.salestorm.dto;

import java.util.Map;

public class MonitoringMetricsDTO {

    private InventoryMetrics inventory;
    private ReservationMetrics reservations;
    private PaymentMetrics payments;
    private OrderMetrics orders;
    private SystemMetrics system;
    private boolean orderServiceAvailable;

    public static class InventoryMetrics {
        private String productName;
        private Long productId;
        private int totalStock;
        private int availableStock;
        private int reservedStock;
        private int soldStock;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getTotalStock() {
            return totalStock;
        }

        public void setTotalStock(int totalStock) {
            this.totalStock = totalStock;
        }

        public int getAvailableStock() {
            return availableStock;
        }

        public void setAvailableStock(int availableStock) {
            this.availableStock = availableStock;
        }

        public int getReservedStock() {
            return reservedStock;
        }

        public void setReservedStock(int reservedStock) {
            this.reservedStock = reservedStock;
        }

        public int getSoldStock() {
            return soldStock;
        }

        public void setSoldStock(int soldStock) {
            this.soldStock = soldStock;
        }
    }

    public static class ReservationMetrics {
        private long total;
        private long active;
        private long expired;
        private long released;
        private long confirmed;
        private long failed;

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getActive() {
            return active;
        }

        public void setActive(long active) {
            this.active = active;
        }

        public long getExpired() {
            return expired;
        }

        public void setExpired(long expired) {
            this.expired = expired;
        }

        public long getReleased() {
            return released;
        }

        public void setReleased(long released) {
            this.released = released;
        }

        public long getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(long confirmed) {
            this.confirmed = confirmed;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }
    }

    public static class PaymentMetrics {
        private long successful;
        private long failed;
        private long pending;
        private long unknown;
        private long refunded;
        private long initiated;

        public long getSuccessful() {
            return successful;
        }

        public void setSuccessful(long successful) {
            this.successful = successful;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }

        public long getUnknown() {
            return unknown;
        }

        public void setUnknown(long unknown) {
            this.unknown = unknown;
        }

        public long getRefunded() {
            return refunded;
        }

        public void setRefunded(long refunded) {
            this.refunded = refunded;
        }

        public long getInitiated() {
            return initiated;
        }

        public void setInitiated(long initiated) {
            this.initiated = initiated;
        }
    }

    public static class OrderMetrics {
        private long created;
        private long paymentPending;
        private long confirmed;
        private long processing;
        private long shipped;
        private long outForDelivery;
        private long delivered;
        private long cancelled;
        private long total;

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public long getPaymentPending() {
            return paymentPending;
        }

        public void setPaymentPending(long paymentPending) {
            this.paymentPending = paymentPending;
        }

        public long getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(long confirmed) {
            this.confirmed = confirmed;
        }

        public long getProcessing() {
            return processing;
        }

        public void setProcessing(long processing) {
            this.processing = processing;
        }

        public long getShipped() {
            return shipped;
        }

        public void setShipped(long shipped) {
            this.shipped = shipped;
        }

        public long getOutForDelivery() {
            return outForDelivery;
        }

        public void setOutForDelivery(long outForDelivery) {
            this.outForDelivery = outForDelivery;
        }

        public long getDelivered() {
            return delivered;
        }

        public void setDelivered(long delivered) {
            this.delivered = delivered;
        }

        public long getCancelled() {
            return cancelled;
        }

        public void setCancelled(long cancelled) {
            this.cancelled = cancelled;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }

    public static class SystemMetrics {
        private long apiRequests;
        private long failedRequests;
        private long kafkaEvents;
        private long pendingEvents;
        private long failedEvents;
        private long processedEvents;

        public long getApiRequests() {
            return apiRequests;
        }

        public void setApiRequests(long apiRequests) {
            this.apiRequests = apiRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public void setFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
        }

        public long getKafkaEvents() {
            return kafkaEvents;
        }

        public void setKafkaEvents(long kafkaEvents) {
            this.kafkaEvents = kafkaEvents;
        }

        public long getPendingEvents() {
            return pendingEvents;
        }

        public void setPendingEvents(long pendingEvents) {
            this.pendingEvents = pendingEvents;
        }

        public long getFailedEvents() {
            return failedEvents;
        }

        public void setFailedEvents(long failedEvents) {
            this.failedEvents = failedEvents;
        }

        public long getProcessedEvents() {
            return processedEvents;
        }

        public void setProcessedEvents(long processedEvents) {
            this.processedEvents = processedEvents;
        }
    }

    public InventoryMetrics getInventory() {
        return inventory;
    }

    public void setInventory(InventoryMetrics inventory) {
        this.inventory = inventory;
    }

    public ReservationMetrics getReservations() {
        return reservations;
    }

    public void setReservations(ReservationMetrics reservations) {
        this.reservations = reservations;
    }

    public PaymentMetrics getPayments() {
        return payments;
    }

    public void setPayments(PaymentMetrics payments) {
        this.payments = payments;
    }

    public OrderMetrics getOrders() {
        return orders;
    }

    public void setOrders(OrderMetrics orders) {
        this.orders = orders;
    }

    public SystemMetrics getSystem() {
        return system;
    }

    public void setSystem(SystemMetrics system) {
        this.system = system;
    }

    public boolean isOrderServiceAvailable() {
        return orderServiceAvailable;
    }

    public void setOrderServiceAvailable(boolean orderServiceAvailable) {
        this.orderServiceAvailable = orderServiceAvailable;
    }
}
