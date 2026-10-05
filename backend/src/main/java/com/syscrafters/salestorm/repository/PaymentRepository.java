package com.syscrafters.salestorm.repository;

import com.syscrafters.salestorm.entity.Payment;
import com.syscrafters.salestorm.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByTransactionReference(String transactionReference);

    Optional<Payment> findByReservationId(Long reservationId);

    List<Payment> findByStatus(PaymentStatus status);

    long countByStatus(PaymentStatus status);
}
