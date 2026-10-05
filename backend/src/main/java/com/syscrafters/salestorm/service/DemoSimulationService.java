package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.SimulationDTOs.LoadSimulationRequest;
import com.syscrafters.salestorm.dto.SimulationDTOs.LoadSimulationResponse;
import com.syscrafters.salestorm.entity.Inventory;
import com.syscrafters.salestorm.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DemoSimulationService {

    private static final Logger log = LoggerFactory.getLogger(DemoSimulationService.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    public DemoSimulationService(InventoryRepository inventoryRepository, InventoryService inventoryService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
    }

    /**
     * Executes real concurrent purchase requests against the atomic reservation SQL query.
     * Uses a thread pool with a starting CountDownLatch so all worker threads strike the DB concurrently!
     */
    public LoadSimulationResponse runLoadSimulation(LoadSimulationRequest req) {
        Long productId = req.getProductId() != null ? req.getProductId() : 1L;
        int initialStock = req.getAvailableStock();
        int totalRequests = req.getConcurrentRequests();
        int quantityPerRequest = req.getQuantityPerRequest();

        // 1. Reset initial stock in DB
        inventoryService.resetInventory(productId, initialStock);

        // 2. Setup concurrency harness
        int threadPoolSize = Math.min(64, Math.max(16, Runtime.getRuntime().availableProcessors() * 4));
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(totalRequests);

        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger rejectedCounter = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    // Wait until all threads are queued up
                    startGate.await();

                    // Execute real atomic reservation against PostgreSQL
                    boolean reserved = inventoryService.reserveStock(productId, quantityPerRequest);
                    if (reserved) {
                        successCounter.incrementAndGet();
                    } else {
                        rejectedCounter.incrementAndGet();
                    }
                } catch (Exception e) {
                    rejectedCounter.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Release the gate! All threads strike the DB concurrently
        startGate.countDown();

        try {
            // Wait up to 60 seconds for completion
            boolean completed = doneGate.await(60, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Simulation timed out waiting for all tasks to finish");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        long duration = System.currentTimeMillis() - startTime;
        if (duration <= 0) duration = 1;

        // 3. Inspect final PostgreSQL state
        Inventory finalInv = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(productId, initialStock));

        int successfulReservations = successCounter.get();
        int rejectedRequests = rejectedCounter.get();
        int reservedUnits = successfulReservations * quantityPerRequest;

        // Invariant checks
        int oversoldQuantity = Math.max(0, reservedUnits - initialStock);
        boolean invariantSatisfied = (oversoldQuantity == 0) && (reservedUnits <= initialStock);

        LoadSimulationResponse resp = new LoadSimulationResponse();
        resp.setInitialAvailableStock(initialStock);
        resp.setTotalRequests(totalRequests);
        resp.setSuccessfulReservations(successfulReservations);
        resp.setRejectedRequests(rejectedRequests);
        resp.setOversoldQuantity(oversoldQuantity);
        resp.setFinalAvailableStock(finalInv.getAvailableQuantity());
        resp.setFinalReservedStock(finalInv.getReservedQuantity());
        resp.setDurationMillis(duration);
        resp.setRequestsPerSecond(Math.round(((double) totalRequests / duration * 1000.0) * 10.0) / 10.0);
        resp.setInvariantSatisfied(invariantSatisfied);
        resp.setMessage(invariantSatisfied
                ? "Test Passed: Atomic database operation prevented overselling under concurrent load."
                : "FAILED: Invariant violated! Check database concurrency isolation.");

        log.info("Simulation completed: Total={}, Success={}, Rejected={}, Oversold={}, Invariant={}",
                totalRequests, successfulReservations, rejectedRequests, oversoldQuantity, invariantSatisfied);

        return resp;
    }
}
