package com.smartcafe.website.controller;

import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLoyaltyInfo(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getLoyaltyInfo(extractId(auth))));
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<?>> getCoupons(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getAvailableCoupons()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getHistory(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getPointsHistory(extractId(auth))));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<?>> redeem(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Integer> body) {
        int points = body.getOrDefault("points", 100);
        return ResponseEntity.ok(ApiResponse.ok("Points redeemed successfully",
                loyaltyService.redeemPoints(extractId(auth), points)));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
