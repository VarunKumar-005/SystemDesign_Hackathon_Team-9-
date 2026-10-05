package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.OrderDTOs.OrderResponse;
import com.syscrafters.salestorm.dto.OrderDTOs.StatusUpdateRequest;
import com.syscrafters.salestorm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Service", description = "Order lifecycle management and state transitions")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders (optionally filtered by customerId)")
    public ApiResponse<List<OrderResponse>> getOrders(@RequestParam(name = "customerId", required = false) Long customerId) {
        if (customerId != null) {
            return ApiResponse.ok(orderService.getOrdersByCustomer(customerId));
        }
        return ApiResponse.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details and full lifecycle status history")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable("id") Long id) {
        return ApiResponse.ok(orderService.getOrderById(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable("id") Long id,
                                                  @RequestParam(name = "reason", required = false) String reason) {
        return ApiResponse.ok("Order cancelled", orderService.cancelOrder(id, reason));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Advance order status along its lifecycle (rejects invalid transitions)")
    public ApiResponse<OrderResponse> updateOrderStatus(@PathVariable("id") Long id,
                                                        @Valid @RequestBody StatusUpdateRequest request) {
        return ApiResponse.ok("Order status updated",
                orderService.updateOrderStatus(id, request.getTargetStatus(), request.getReason()));
    }
}
