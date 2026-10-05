package com.syscrafters.salestorm.scheduler;

import com.syscrafters.salestorm.entity.OutboxEvent;
import com.syscrafters.salestorm.service.KafkaEventBus;
import com.syscrafters.salestorm.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxDispatcherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcherScheduler.class);

    private final OutboxService outboxService;
    private final KafkaEventBus kafkaEventBus;

    public OutboxDispatcherScheduler(OutboxService outboxService, KafkaEventBus kafkaEventBus) {
        this.outboxService = outboxService;
        this.kafkaEventBus = kafkaEventBus;
    }

    /**
     * Polls pending outbox events every 2 seconds and publishes to Kafka / Event bus.
     */
    @Scheduled(fixedDelay = 2000)
    public void dispatchPendingOutboxEvents() {
        try {
            List<OutboxEvent> pending = outboxService.getPendingEvents();
            for (OutboxEvent event : pending) {
                try {
                    boolean success = kafkaEventBus.publish(event);
                    if (success) {
                        outboxService.markPublished(event.getId());
                    } else {
                        outboxService.markFailed(event.getId(), "Publish returned false");
                    }
                } catch (Exception e) {
                    log.error("Failed to dispatch outbox event [{}]: {}", event.getId(), e.getMessage());
                    outboxService.markFailed(event.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Outbox dispatch poll skipped: {}", e.getMessage());
        }
    }
}
