package com.syscrafters.salestorm.scheduler;

import com.syscrafters.salestorm.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ReservationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpiryScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Polls every 3 seconds for expired reservations and releases inventory atomically.
     */
    @Scheduled(fixedDelay = 3000)
    public void cleanupExpiredReservations() {
        try {
            int releasedCount = reservationService.processExpiredReservations();
            if (releasedCount > 0) {
                log.info("ReservationExpiryScheduler released [{}] expired reservations back to inventory", releasedCount);
            }
        } catch (Exception e) {
            log.error("Error in ReservationExpiryScheduler: {}", e.getMessage());
        }
    }
}
