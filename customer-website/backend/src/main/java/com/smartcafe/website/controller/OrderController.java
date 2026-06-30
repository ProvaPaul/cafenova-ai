package com.smartcafe.website.controller;

import com.smartcafe.website.dto.request.CheckoutRequest;
import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.dto.response.OrderResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader("Authorization") String auth,
            @RequestBody CheckoutRequest req) {
        Long customerId = extractId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully", orderService.checkout(customerId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> history(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getHistory(extractId(auth))));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(extractId(auth), orderId)));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
