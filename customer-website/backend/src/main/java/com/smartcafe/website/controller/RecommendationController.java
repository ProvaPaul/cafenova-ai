package com.smartcafe.website.controller;

import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.dto.response.RecommendationResponse;
import com.smartcafe.website.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /** Trending items — no authentication required */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<?>> getTrending(
            @RequestParam(defaultValue = "5") int limit) {
        RecommendationResponse result = recommendationService.getTrending(limit);
        return ResponseEntity.ok(ApiResponse.success("Trending recommendations", result));
    }

    /** Personalised for the logged-in customer */
    @GetMapping("/personal")
    public ResponseEntity<ApiResponse<?>> getPersonal(
            Authentication auth,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "home") String context) {
        Long customerId = extractCustomerId(auth);
        RecommendationResponse result = recommendationService.getPersonal(customerId, limit, context);
        return ResponseEntity.ok(ApiResponse.success("Personal recommendations", result));
    }

    /** Similar items to a given product (product detail page) */
    @GetMapping("/similar/{productId}")
    public ResponseEntity<ApiResponse<?>> getSimilar(
            @PathVariable int productId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "5") int limit) {
        RecommendationResponse result = recommendationService.getSimilar(productId, name, category, limit);
        return ResponseEntity.ok(ApiResponse.success("Similar items", result));
    }

    /** Cart-based recommendations */
    @PostMapping("/cart")
    public ResponseEntity<ApiResponse<?>> getCartRecommendations(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.getOrDefault("productIds", List.of());
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) body.getOrDefault("productNames", List.of());
        int limit = (int) body.getOrDefault("limit", 5);
        RecommendationResponse result = recommendationService.getCartRecommendations(ids, names, limit);
        return ResponseEntity.ok(ApiResponse.success("Cart recommendations", result));
    }

    /** POS recommendations — called by Java Swing cashier app */
    @PostMapping("/pos")
    public ResponseEntity<ApiResponse<?>> getPosRecommendations(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.getOrDefault("itemIds", List.of());
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) body.getOrDefault("itemNames", List.of());
        int limit = (int) body.getOrDefault("limit", 4);
        RecommendationResponse result = recommendationService.getPosRecommendations(ids, names, limit);
        return ResponseEntity.ok(ApiResponse.success("POS recommendations", result));
    }

    // ── Stub analytics endpoint for future admin use ──────────────────────────
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<ApiResponse<?>> getAnalyticsDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Analytics dashboard (AI stub)",
                Map.of("status", "stub", "message", "Connect to Python AI service for live analytics")));
    }

    private Long extractCustomerId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        try { return Long.parseLong(auth.getName()); } catch (Exception e) { return null; }
    }
}
