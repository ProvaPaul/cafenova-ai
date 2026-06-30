package com.smartcafe.website.controller;

import com.smartcafe.website.dto.request.FeedbackRequest;
import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> submit(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody FeedbackRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Feedback submitted", feedbackService.submit(extractId(auth), req)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> myFeedback(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getMyFeedback(extractId(auth))));
    }

    @GetMapping("/public/item/{menuItemId}")
    public ResponseEntity<ApiResponse<?>> itemFeedback(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getItemFeedback(menuItemId)));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
