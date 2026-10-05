package com.syscrafters.salestorm.repository;

import com.syscrafters.salestorm.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventIdAndConsumerGroup(Long eventId, String consumerGroup);
    Optional<ProcessedEvent> findByEventIdAndConsumerGroup(Long eventId, String consumerGroup);
}
