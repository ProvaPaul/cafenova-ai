package com.smartcafe.website.controller;

import com.smartcafe.website.dto.request.ChangePasswordRequest;
import com.smartcafe.website.dto.request.UpdateProfileRequest;
import com.smartcafe.website.dto.response.ApiResponse;
import com.smartcafe.website.security.JwtTokenProvider;
import com.smartcafe.website.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProfile(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(extractId(auth))));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", profileService.updateProfile(extractId(auth), req)));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody ChangePasswordRequest req) {
        profileService.changePassword(extractId(auth), req);
        return ResponseEntity.ok(ApiResponse.ok("Password changed", null));
    }

    private Long extractId(String auth) {
        return jwtTokenProvider.getCustomerIdFromToken(auth.substring(7));
    }
}
