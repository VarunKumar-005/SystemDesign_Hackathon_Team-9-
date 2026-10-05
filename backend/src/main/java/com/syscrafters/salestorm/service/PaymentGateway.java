package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.entity.Payment.PaymentStatus;
import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentGatewayResult processPayment(String transactionRef, BigDecimal amount, String currency, String scenario);

    record PaymentGatewayResult(PaymentStatus status, String gatewayTransactionId, String message) {}
}
