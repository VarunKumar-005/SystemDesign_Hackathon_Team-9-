package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.MonitoringMetricsDTO;
import com.syscrafters.salestorm.dto.SimulationDTOs.LoadSimulationRequest;
import com.syscrafters.salestorm.dto.SimulationDTOs.LoadSimulationResponse;
import com.syscrafters.salestorm.service.DemoSimulationService;
import com.syscrafters.salestorm.service.EventConsumerService;
import com.syscrafters.salestorm.service.InventoryService;
import com.syscrafters.salestorm.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin & Monitoring", description = "Live metrics, concurrency simulation load tests, and demo scenario controls")
public class AdminMonitoringController {

    private final MonitoringService monitoringService;
    private final DemoSimulationService simulationService;
    private final EventConsumerService eventConsumerService;
    private final InventoryService inventoryService;

    public AdminMonitoringController(MonitoringService monitoringService,
                                     DemoSimulationService simulationService,
                                     EventConsumerService eventConsumerService,
                                     InventoryService inventoryService) {
        this.monitoringService = monitoringService;
        this.simulationService = simulationService;
        this.eventConsumerService = eventConsumerService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get live system dashboard metrics (Inventory, Reservations, Payments, Orders, Kafka/Outbox)")
    public ApiResponse<MonitoringMetricsDTO> getMetrics() {
        return ApiResponse.ok(monitoringService.getMetrics());
    }

    @PostMapping("/simulate-load")
    @Operation(summary = "Execute real multi-threaded concurrent flash-sale load simulation")
    public ApiResponse<LoadSimulationResponse> simulateLoad(@Valid @RequestBody LoadSimulationRequest request) {
        LoadSimulationResponse response = simulationService.runLoadSimulation(request);
        return ApiResponse.ok("Load test completed", response);
    }

    @PostMapping("/order-service-toggle")
    @Operation(summary = "Toggle Order Service availability (Simulates Order Service Down scenario)")
    public ApiResponse<Map<String, Object>> toggleOrderService(@RequestParam(name = "available", required = false) Boolean available) {
        boolean newState;
        if (available != null) {
            eventConsumerService.setOrderServiceAvailable(available);
            newState = available;
        } else {
            newState = !eventConsumerService.isOrderServiceAvailable();
            eventConsumerService.setOrderServiceAvailable(newState);
        }
        return ApiResponse.ok("Order Service availability set", Map.of(
                "orderServiceAvailable", newState,
                "statusDescription", newState ? "ONLINE - Processing events normally" : "OFFLINE - Events queue in Outbox/Kafka"
        ));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset inventory stock to default (100 units)")
    public ApiResponse<String> resetInventory(@RequestParam(name = "productId", defaultValue = "1") Long productId,
                                              @RequestParam(name = "stock", defaultValue = "100") int stock) {
        inventoryService.resetInventory(productId, stock);
        return ApiResponse.ok("Stock for product " + productId + " successfully reset to " + stock + " units.", null);
    }
}
