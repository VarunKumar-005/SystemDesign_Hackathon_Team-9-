package com.syscrafters.salestorm.repository;

import com.syscrafters.salestorm.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    /**
     * ATOMIC INVENTORY RESERVATION:
     * Does NOT do READ -> check -> WRITE.
     * Decrements available and increments reserved in a single atomic SQL statement
     * with the predicate `available_quantity >= :quantity`.
     * Returns number of affected rows:
     * 1 = reservation successful
     * 0 = insufficient stock
     */
    @Modifying
    @Query(value = "UPDATE inventory " +
                   "SET available_quantity = available_quantity - :quantity, " +
                   "    reserved_quantity = reserved_quantity + :quantity, " +
                   "    updated_at = CURRENT_TIMESTAMP " +
                   "WHERE product_id = :productId " +
                   "AND available_quantity >= :quantity", nativeQuery = true)
    int reserveStockAtomic(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * ATOMIC RESERVATION RELEASE:
     * Returns reserved stock back to available stock.
     */
    @Modifying
    @Query(value = "UPDATE inventory " +
                   "SET available_quantity = available_quantity + :quantity, " +
                   "    reserved_quantity = reserved_quantity - :quantity, " +
                   "    updated_at = CURRENT_TIMESTAMP " +
                   "WHERE product_id = :productId " +
                   "AND reserved_quantity >= :quantity", nativeQuery = true)
    int releaseStockAtomic(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * ATOMIC ORDER CONFIRMATION:
     * Moves reserved stock to sold stock after successful payment.
     */
    @Modifying
    @Query(value = "UPDATE inventory " +
                   "SET reserved_quantity = reserved_quantity - :quantity, " +
                   "    sold_quantity = sold_quantity + :quantity, " +
                   "    updated_at = CURRENT_TIMESTAMP " +
                   "WHERE product_id = :productId " +
                   "AND reserved_quantity >= :quantity", nativeQuery = true)
    int confirmSoldAtomic(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Reset stock for demo testing.
     */
    @Modifying
    @Query(value = "UPDATE inventory " +
                   "SET total_quantity = :stock, " +
                   "    available_quantity = :stock, " +
                   "    reserved_quantity = 0, " +
                   "    sold_quantity = 0, " +
                   "    updated_at = CURRENT_TIMESTAMP " +
                   "WHERE product_id = :productId", nativeQuery = true)
    int resetStock(@Param("productId") Long productId, @Param("stock") int stock);
}
