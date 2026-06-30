package com.smartcafe.website.controller;

import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getAll(extractId(auth))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<?>> unreadCount(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(extractId(auth))));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @RequestHeader("Authorization") String auth) {
        notificationService.markAllRead(extractId(auth));
        return ResponseEntity.ok(ApiResponse.ok("All marked as read", null));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id) {
        notificationService.markRead(extractId(auth), id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
