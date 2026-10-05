package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.entity.Payment.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallbackProcessPayment")
    public PaymentGatewayResult processPayment(String transactionRef, BigDecimal amount, String currency, String scenario) {
        String gatewayTxId = "GW-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        if ("FAILURE".equalsIgnoreCase(scenario)) {
            log.info("Simulating Payment FAILURE for ref [{}]", transactionRef);
            return new PaymentGatewayResult(PaymentStatus.FAILED, gatewayTxId, "Card declined: Simulated payment failure scenario");
        }

        if ("TIMEOUT".equalsIgnoreCase(scenario)) {
            log.warn("Simulating Payment TIMEOUT for ref [{}] - returning UNKNOWN status", transactionRef);
            // Requirement #9: status must become UNKNOWN, NOT FAILED
            return new PaymentGatewayResult(PaymentStatus.UNKNOWN, gatewayTxId, "Gateway response timed out. Transaction status is UNKNOWN pending reconciliation.");
        }

        // Default: SUCCESS
        log.info("Simulating Payment SUCCESS for ref [{}]", transactionRef);
        return new PaymentGatewayResult(PaymentStatus.SUCCEEDED, gatewayTxId, "Payment successfully authorized and captured");
    }

    /**
     * Resilience4j Circuit Breaker Fallback
     */
    public PaymentGatewayResult fallbackProcessPayment(String transactionRef, BigDecimal amount, String currency, String scenario, Throwable t) {
        log.error("Resilience4j Circuit Breaker OPEN or execution failed: {}", t.getMessage());
        return new PaymentGatewayResult(PaymentStatus.UNKNOWN, "GW-FALLBACK", "Payment gateway circuit open or degraded: " + t.getMessage());
    }
}
