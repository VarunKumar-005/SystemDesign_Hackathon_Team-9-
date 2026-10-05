package com.syscrafters.salestorm.repository;

import com.syscrafters.salestorm.entity.Order;
import com.syscrafters.salestorm.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByPaymentId(Long paymentId);

    Optional<Order> findByCheckoutSessionId(Long checkoutSessionId);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findAllByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);
}
