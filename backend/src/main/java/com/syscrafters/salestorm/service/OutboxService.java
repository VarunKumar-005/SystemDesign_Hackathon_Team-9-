package com.syscrafters.salestorm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscrafters.salestorm.entity.OutboxEvent;
import com.syscrafters.salestorm.entity.OutboxEvent.OutboxStatus;
import com.syscrafters.salestorm.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Save event to Outbox table within the caller's active database transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent publishEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxEvent event = new OutboxEvent(aggregateType, aggregateId, eventType, jsonPayload);
            return outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> getPendingEvents() {
        return outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }

    @Transactional
    public void markPublished(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            outboxEventRepository.save(event);
        });
    }

    @Transactional
    public void markFailed(Long eventId, String errorMessage) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setErrorMessage(errorMessage);
            if (event.getRetryCount() >= 5) {
                event.setStatus(OutboxStatus.FAILED);
            }
            outboxEventRepository.save(event);
        });
    }

    public long getPendingCount() {
        return outboxEventRepository.countByStatus(OutboxStatus.PENDING);
    }

    public long getPublishedCount() {
        return outboxEventRepository.countByStatus(OutboxStatus.PUBLISHED);
    }

    public long getFailedCount() {
        return outboxEventRepository.countByStatus(OutboxStatus.FAILED);
    }

    public long getTotalCount() {
        return outboxEventRepository.count();
    }
}
