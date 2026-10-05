package com.syscrafters.salestorm.service;

import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayFactory {

    private final MockPaymentGateway mockPaymentGateway;

    public PaymentGatewayFactory(MockPaymentGateway mockPaymentGateway) {
        this.mockPaymentGateway = mockPaymentGateway;
    }

    public PaymentGateway getPaymentGateway(String paymentMethod) {
        // Prototype uses Mock Payment Gateway
        return mockPaymentGateway;
    }
}
