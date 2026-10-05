package com.syscrafters.salestorm.dto;

import jakarta.validation.constraints.Min;

public class SimulationDTOs {

    public static class LoadSimulationRequest {
        private Long productId = 1L;

        @Min(value = 1, message = "Available stock must be at least 1")
        private int availableStock = 100;

        @Min(value = 1, message = "Concurrent requests must be at least 1")
        private int concurrentRequests = 1000;

        @Min(value = 1, message = "Quantity per request must be at least 1")
        private int quantityPerRequest = 1;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getAvailableStock() {
            return availableStock;
        }

        public void setAvailableStock(int availableStock) {
            this.availableStock = availableStock;
        }

        public int getConcurrentRequests() {
            return concurrentRequests;
        }

        public void setConcurrentRequests(int concurrentRequests) {
            this.concurrentRequests = concurrentRequests;
        }

        public int getQuantityPerRequest() {
            return quantityPerRequest;
        }

        public void setQuantityPerRequest(int quantityPerRequest) {
            this.quantityPerRequest = quantityPerRequest;
        }
    }

    public static class LoadSimulationResponse {
        private int initialAvailableStock;
        private int totalRequests;
        private int successfulReservations;
        private int rejectedRequests;
        private int oversoldQuantity;
        private int finalAvailableStock;
        private int finalReservedStock;
        private long durationMillis;
        private double requestsPerSecond;
        private boolean invariantSatisfied;
        private String message;

        public int getInitialAvailableStock() {
            return initialAvailableStock;
        }

        public void setInitialAvailableStock(int initialAvailableStock) {
            this.initialAvailableStock = initialAvailableStock;
        }

        public int getTotalRequests() {
            return totalRequests;
        }

        public void setTotalRequests(int totalRequests) {
            this.totalRequests = totalRequests;
        }

        public int getSuccessfulReservations() {
            return successfulReservations;
        }

        public void setSuccessfulReservations(int successfulReservations) {
            this.successfulReservations = successfulReservations;
        }

        public int getRejectedRequests() {
            return rejectedRequests;
        }

        public void setRejectedRequests(int rejectedRequests) {
            this.rejectedRequests = rejectedRequests;
        }

        public int getOversoldQuantity() {
            return oversoldQuantity;
        }

        public void setOversoldQuantity(int oversoldQuantity) {
            this.oversoldQuantity = oversoldQuantity;
        }

        public int getFinalAvailableStock() {
            return finalAvailableStock;
        }

        public void setFinalAvailableStock(int finalAvailableStock) {
            this.finalAvailableStock = finalAvailableStock;
        }

        public int getFinalReservedStock() {
            return finalReservedStock;
        }

        public void setFinalReservedStock(int finalReservedStock) {
            this.finalReservedStock = finalReservedStock;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public void setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
        }

        public double getRequestsPerSecond() {
            return requestsPerSecond;
        }

        public void setRequestsPerSecond(double requestsPerSecond) {
            this.requestsPerSecond = requestsPerSecond;
        }

        public boolean isInvariantSatisfied() {
            return invariantSatisfied;
        }

        public void setInvariantSatisfied(boolean invariantSatisfied) {
            this.invariantSatisfied = invariantSatisfied;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
