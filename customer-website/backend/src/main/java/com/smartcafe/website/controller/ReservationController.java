package com.smartcafe.website.controller;

import com.smartcafe.website.dto.request.ReservationRequest;
import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/tables")
    public ResponseEntity<ApiResponse<?>> getTables() {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getAvailableTables()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody ReservationRequest req) {
        Long customerId = extractId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Reservation submitted", reservationService.create(customerId, req)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> myReservations(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.getMyReservations(extractId(auth))));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancel(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Reservation cancelled", reservationService.cancel(extractId(auth), id)));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
