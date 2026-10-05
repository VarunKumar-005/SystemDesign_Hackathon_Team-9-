package com.syscrafters.salestorm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syscrafters.salestorm.entity.OutboxEvent;
import com.syscrafters.salestorm.entity.ProcessedEvent;
import com.syscrafters.salestorm.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);
    private static final String CONSUMER_GROUP = "salestorm-order-consumer";

    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    // Failure Scenario #11: Toggle for Order Service Availability
    private final AtomicBoolean orderServiceAvailable = new AtomicBoolean(true);

    public EventConsumerService(ProcessedEventRepository processedEventRepository,
                                ObjectMapper objectMapper,
                                @Lazy OrderService orderService,
                                @Lazy InventoryService inventoryService) {
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
    }

    public boolean isOrderServiceAvailable() {
        return orderServiceAvailable.get();
    }

    public void setOrderServiceAvailable(boolean available) {
        this.orderServiceAvailable.set(available);
        log.info("Order Service availability set to: {}", available);
    }

    /**
     * Local event listener for Outbox events dispatched by KafkaEventBus.
     */
    @EventListener
    @Transactional
    public void handleLocalOutboxEvent(OutboxEvent event) {
        consumeEvent(event.getId(), event.getEventType(), event.getPayload());
    }

    /**
     * Kafka listener (when Kafka broker is connected)
     */
    @KafkaListener(topics = "salestorm-events", groupId = CONSUMER_GROUP, autoStartup = "false")
    @Transactional
    public void handleKafkaMessage(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            Long eventId = root.has("eventId") ? root.get("eventId").asLong() : null;
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";
            consumeEvent(eventId, eventType, message);
        } catch (Exception e) {
            log.error("Failed to parse Kafka message: {}", message, e);
        }
    }

    /**
     * IDEMPOTENT EVENT CONSUMPTION:
     * 1. Checks PROCESSED_EVENT table with (event_id, consumer_group).
     * 2. If already processed, skips to prevent duplicate orders/payments!
     * 3. Checks Order Service availability toggle (Failure Scenario #11).
     * 4. If unavailable, skips execution so event remains unconsumed until service recovers!
     */
    @Transactional
    public synchronized boolean consumeEvent(Long eventId, String eventType, String payloadJson) {
        if (eventId != null && processedEventRepository.existsByEventIdAndConsumerGroup(eventId, CONSUMER_GROUP)) {
            log.info("Duplicate event [{}] for group [{}]. Skipping duplicate consumption.", eventId, CONSUMER_GROUP);
            return false;
        }

        try {
            JsonNode payload = objectMapper.readTree(payloadJson);

            switch (eventType) {
                case "PaymentSucceeded":
                    // Check if Order Service is simulated as down
                    if (!orderServiceAvailable.get()) {
                        log.warn("Order Service is DOWN! PaymentSucceeded event [{}] delayed. Order is NOT lost; will process upon recovery.", eventId);
                        return false;
                    }
                    Long checkoutSessionId = payload.has("checkoutSessionId") ? payload.get("checkoutSessionId").asLong() : null;
                    Long paymentId = payload.has("paymentId") ? payload.get("paymentId").asLong() : null;
                    if (checkoutSessionId != null && paymentId != null) {
                        orderService.createOrderFromPayment(checkoutSessionId, paymentId);
                    }
                    break;

                case "ReservationExpired":
                    Long reservationId = payload.has("reservationId") ? payload.get("reservationId").asLong() : null;
                    Long productId = payload.has("productId") ? payload.get("productId").asLong() : null;
                    int quantity = payload.has("quantity") ? payload.get("quantity").asInt() : 0;
                    if (productId != null && quantity > 0) {
                        inventoryService.releaseStock(productId, quantity);
                    }
                    break;

                case "PaymentRefunded":
                    log.info("Processing PaymentRefunded event");
                    break;

                default:
                    log.info("Event [{}] received: {}", eventType, eventId);
                    break;
            }

            // Record into PROCESSED_EVENT table for idempotency
            if (eventId != null) {
                processedEventRepository.save(new ProcessedEvent(eventId, CONSUMER_GROUP));
            }
            return true;
        } catch (Exception e) {
            log.error("Error consuming event [{}]: {}", eventId, e.getMessage(), e);
            return false;
        }
    }

    public long getProcessedCount() {
        return processedEventRepository.count();
    }
}
