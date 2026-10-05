package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.entity.Inventory;
import com.syscrafters.salestorm.exception.InsufficientStockException;
import com.syscrafters.salestorm.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final String REDIS_STOCK_PREFIX = "stock:product:";

    private final InventoryRepository inventoryRepository;
    private final StringRedisTemplate redisTemplate;

    public InventoryService(InventoryRepository inventoryRepository,
                            @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * ATOMIC INVENTORY RESERVATION
     * PostgreSQL is the source of truth.
     * Executes atomic single-statement update preventing race conditions.
     */
    @Transactional
    public boolean reserveStock(Long productId, int quantity) {
        int rowsUpdated = inventoryRepository.reserveStockAtomic(productId, quantity);

        if (rowsUpdated == 1) {
            log.info("Successfully reserved [{}] units for product [{}]", quantity, productId);
            syncRedisCache(productId);
            return true;
        } else {
            log.warn("Reservation failed for product [{}]: Insufficient available stock for quantity [{}]", productId, quantity);
            return false;
        }
    }

    /**
     * ATOMIC INVENTORY RELEASE
     * Returns reserved stock back to available stock.
     */
    @Transactional
    public boolean releaseStock(Long productId, int quantity) {
        int rowsUpdated = inventoryRepository.releaseStockAtomic(productId, quantity);
        if (rowsUpdated == 1) {
            log.info("Released [{}] reserved units back to available stock for product [{}]", quantity, productId);
            syncRedisCache(productId);
            return true;
        }
        log.warn("Stock release failed for product [{}], quantity [{}]", productId, quantity);
        return false;
    }

    /**
     * ATOMIC ORDER CONFIRMATION
     * Converts reserved stock to sold stock upon successful payment.
     */
    @Transactional
    public boolean confirmSold(Long productId, int quantity) {
        int rowsUpdated = inventoryRepository.confirmSoldAtomic(productId, quantity);
        if (rowsUpdated == 1) {
            log.info("Confirmed [{}] sold units for product [{}]", quantity, productId);
            syncRedisCache(productId);
            return true;
        }
        log.warn("Confirm sold failed for product [{}], quantity [{}]", productId, quantity);
        return false;
    }

    @Transactional(readOnly = true)
    public Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InsufficientStockException("Inventory not found for product: " + productId));
    }

    @Transactional
    public void resetInventory(Long productId, int stock) {
        inventoryRepository.resetStock(productId, stock);
        syncRedisCache(productId);
        log.info("Reset stock for product [{}] to [{}]", productId, stock);
    }

    /**
     * Helper to mirror stock in Redis for read-through caches.
     * Redis is NEVER the source of truth for stock availability.
     */
    public void syncRedisCache(Long productId) {
        if (redisTemplate != null) {
            try {
                inventoryRepository.findByProductId(productId).ifPresent(inv -> {
                    redisTemplate.opsForValue().set(
                            REDIS_STOCK_PREFIX + productId,
                            String.valueOf(inv.getAvailableQuantity()),
                            Duration.ofMinutes(10)
                    );
                });
            } catch (Exception e) {
                log.debug("Redis cache sync skipped: {}", e.getMessage());
            }
        }
    }
}
