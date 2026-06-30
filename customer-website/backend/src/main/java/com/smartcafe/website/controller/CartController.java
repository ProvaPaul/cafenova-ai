package com.smartcafe.website.controller;

import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCart(
            @RequestHeader("Authorization") String auth) {
        Long customerId = extractId(auth);
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(customerId)));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addItem(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Integer> body) {
        Long customerId = extractId(auth);
        Long menuItemId = Long.valueOf(body.get("menuItemId"));
        int qty = body.getOrDefault("quantity", 1);
        return ResponseEntity.ok(ApiResponse.ok("Added to cart", cartService.addItem(customerId, menuItemId, qty)));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<?>> updateQty(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> body) {
        Long customerId = extractId(auth);
        var result = cartService.updateQuantity(customerId, cartItemId, body.get("quantity"));
        return ResponseEntity.ok(result == null ? ApiResponse.ok("Removed", null) : ApiResponse.ok(result));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long cartItemId) {
        cartService.removeItem(extractId(auth), cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Removed", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("Authorization") String auth) {
        cartService.clearCart(extractId(auth));
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", null));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
