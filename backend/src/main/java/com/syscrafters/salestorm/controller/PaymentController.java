package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.PaymentDTOs.PaymentProcessRequest;
import com.syscrafters.salestorm.dto.PaymentDTOs.PaymentResponse;
import com.syscrafters.salestorm.dto.PaymentDTOs.ReconciliationRequest;
import com.syscrafters.salestorm.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Service", description = "Idempotent payment processing, timeout reconciliation, and mock callbacks")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Process idempotent payment")
    public ApiResponse<PaymentResponse> processPayment(@Valid @RequestBody PaymentProcessRequest request) {
        return ApiResponse.ok(paymentService.processPayment(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment details by ID")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable("id") Long id) {
        return ApiResponse.ok(paymentService.getPaymentById(id));
    }

    @PostMapping("/callbacks/mock")
    @Operation(summary = "Mock Payment Gateway webhook callback")
    public ApiResponse<String> mockCallback(@RequestBody String payload) {
        return ApiResponse.ok("Mock callback processed successfully", payload);
    }

    @PostMapping("/reconcile")
    @Operation(summary = "Reconcile payment in UNKNOWN / TIMEOUT status to SUCCEEDED or FAILED")
    public ApiResponse<PaymentResponse> reconcilePayment(@Valid @RequestBody ReconciliationRequest request) {
        return ApiResponse.ok("Payment reconciled", paymentService.reconcilePayment(request));
    }
}
