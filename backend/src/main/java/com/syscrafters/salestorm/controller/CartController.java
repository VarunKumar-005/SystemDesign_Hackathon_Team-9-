package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.CartDTOs.AddToCartRequest;
import com.syscrafters.salestorm.dto.CartDTOs.CartResponse;
import com.syscrafters.salestorm.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart Service", description = "Cart operations with stock boundary enforcement")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer cart with total and stock status")
    public ApiResponse<CartResponse> getCart(@PathVariable("customerId") Long customerId) {
        return ApiResponse.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart (preventing exceeding available stock)")
    public ApiResponse<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        return ApiResponse.ok("Item added to cart", cartService.addToCart(request));
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Clear customer cart")
    public ApiResponse<String> clearCart(@PathVariable("customerId") Long customerId) {
        cartService.clearCart(customerId);
        return ApiResponse.ok("Cart cleared", null);
    }
}
