package com.syscrafters.salestorm.config;

import com.syscrafters.salestorm.entity.Customer;
import com.syscrafters.salestorm.entity.Customer.CustomerRole;
import com.syscrafters.salestorm.entity.Inventory;
import com.syscrafters.salestorm.entity.Product;
import com.syscrafters.salestorm.repository.CustomerRepository;
import com.syscrafters.salestorm.repository.InventoryRepository;
import com.syscrafters.salestorm.repository.ProductRepository;
import com.syscrafters.salestorm.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;

    public DataInitializer(ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           CustomerRepository customerRepository,
                           InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.customerRepository = customerRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public void run(String... args) {
        // Seed default Customer & Admin
        if (!customerRepository.existsByEmail("customer@salestorm.io")) {
            customerRepository.save(new Customer("customer@salestorm.io", "password123", "Demo Customer", CustomerRole.CUSTOMER));
            log.info("Seeded Customer: customer@salestorm.io / password123");
        }
        if (!customerRepository.existsByEmail("admin@salestorm.io")) {
            customerRepository.save(new Customer("admin@salestorm.io", "admin123", "SysCrafters Admin", CustomerRole.ADMIN));
            log.info("Seeded Admin: admin@salestorm.io / admin123");
        }

        // Seed Flash-Sale Product
        if (productRepository.count() == 0) {
            Product headphone = new Product(
                    "Premium Wireless Headphones",
                    "High-fidelity active noise cancellation, 40-hour battery life, ultra-low latency audio, and ergonomic comfort cushions. Perfect for music enthusiasts and competitive gamers alike.",
                    BigDecimal.valueOf(2999.00),
                    BigDecimal.valueOf(1499.00),
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80",
                    true,
                    300 // 5 minutes reservation duration
            );
            headphone.setSaleStartTime(LocalDateTime.now().minusHours(1));
            headphone.setSaleEndTime(LocalDateTime.now().plusDays(2));
            headphone = productRepository.save(headphone);

            // Seed 100 Initial Stock in Inventory
            Inventory inventory = new Inventory(headphone.getId(), 100);
            inventoryRepository.save(inventory);
            inventoryService.syncRedisCache(headphone.getId());

            log.info("Seeded Flash Sale Product [{}] with 100 units initial stock in PostgreSQL and synced to Redis.", headphone.getName());
        }
    }
}
