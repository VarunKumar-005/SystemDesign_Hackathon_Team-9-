package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.entity.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KafkaEventBus {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);
    private static final String DEFAULT_TOPIC = "salestorm-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ApplicationEventPublisher localEventPublisher;

    public KafkaEventBus(@Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate,
                         ApplicationEventPublisher localEventPublisher) {
        this.kafkaTemplate = kafkaTemplate;
        this.localEventPublisher = localEventPublisher;
    }

    public boolean publish(OutboxEvent event) {
        boolean sentToKafka = false;
        if (kafkaTemplate != null) {
            try {
                // Attempt publishing to Kafka broker (with 500ms timeout so local run without Kafka doesn't block)
                kafkaTemplate.send(DEFAULT_TOPIC, event.getAggregateId(), event.getPayload())
                        .get(500, TimeUnit.MILLISECONDS);
                log.info("Successfully sent event [{}] to Kafka topic {}", event.getEventType(), DEFAULT_TOPIC);
                sentToKafka = true;
            } catch (Exception e) {
                log.debug("Kafka broker not reachable or timed out, falling back to local bus: {}", e.getMessage());
            }
        }

        // Always publish to local event listeners (idempotent consumers)
        localEventPublisher.publishEvent(event);
        return true;
    }
}
