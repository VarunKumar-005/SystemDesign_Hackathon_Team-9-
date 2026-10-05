package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.ReservationDTOs.ReservationRequest;
import com.syscrafters.salestorm.dto.ReservationDTOs.ReservationResponse;
import com.syscrafters.salestorm.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation Service", description = "Atomic inventory reservation and expiry lifecycle")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Atomically reserve product inventory")
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ApiResponse.ok("Stock successfully reserved", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details and remaining countdown")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable("id") Long id) {
        return ApiResponse.ok(reservationService.getReservation(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel and release reservation back to available inventory")
    public ApiResponse<String> cancelReservation(@PathVariable("id") Long id) {
        reservationService.cancelReservation(id);
        return ApiResponse.ok("Reservation cancelled and inventory released", null);
    }

    @PostMapping("/expire-check")
    @Operation(summary = "Trigger immediate check and release of expired reservations")
    public ApiResponse<Integer> triggerExpiryCheck() {
        int released = reservationService.processExpiredReservations();
        return ApiResponse.ok("Processed expired reservations. Released count: " + released, released);
    }
}
