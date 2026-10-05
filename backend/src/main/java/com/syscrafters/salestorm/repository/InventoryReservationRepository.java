package com.syscrafters.salestorm.repository;

import com.syscrafters.salestorm.entity.InventoryReservation;
import com.syscrafters.salestorm.entity.InventoryReservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    List<InventoryReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime threshold);

    List<InventoryReservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByStatus(ReservationStatus status);

    @Query("SELECT r FROM InventoryReservation r WHERE r.status IN ('RESERVED', 'PAYMENT_PENDING') AND r.expiresAt > :now")
    List<InventoryReservation> findActiveReservations(@Param("now") LocalDateTime now);
}
