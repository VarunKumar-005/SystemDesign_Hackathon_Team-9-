package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.CheckoutDTOs.CheckoutInitRequest;
import com.syscrafters.salestorm.dto.CheckoutDTOs.CheckoutResponse;
import com.syscrafters.salestorm.dto.PaymentDTOs.PaymentProcessRequest;
import com.syscrafters.salestorm.dto.PaymentDTOs.PaymentResponse;
import com.syscrafters.salestorm.service.CheckoutService;
import com.syscrafters.salestorm.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkouts")
@Tag(name = "Checkout Service", description = "Checkout session orchestration facade")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final PaymentService paymentService;

    public CheckoutController(CheckoutService checkoutService, PaymentService paymentService) {
        this.checkoutService = checkoutService;
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initialize checkout session from an active reservation")
    public ApiResponse<CheckoutResponse> createCheckoutSession(@Valid @RequestBody CheckoutInitRequest request) {
        return ApiResponse.ok(checkoutService.initiateCheckout(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get checkout session details and countdown")
    public ApiResponse<CheckoutResponse> getCheckoutSession(@PathVariable("id") Long id) {
        return ApiResponse.ok(checkoutService.getCheckoutSession(id));
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Execute idempotent payment against checkout session")
    public ApiResponse<PaymentResponse> processPaymentForCheckout(
            @PathVariable("id") Long checkoutId,
            @Valid @RequestBody PaymentProcessRequest request) {
        request.setCheckoutSessionId(checkoutId);
        PaymentResponse response = paymentService.processPayment(request);
        return ApiResponse.ok("Payment processed", response);
    }
}
