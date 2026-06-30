package com.smartcafe.website.service;

import com.smartcafe.website.dto.request.*;
import com.smartcafe.website.dto.response.AuthResponse;
import com.smartcafe.website.entity.CustomerEntity;
import com.smartcafe.website.repository.CustomerRepository;
import com.smartcafe.website.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (customerRepository.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already taken");
        if (customerRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered");

        CustomerEntity customer = CustomerEntity.builder()
                .fullName(req.getFullName())
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .phone(req.getPhone())
                .active(true)
                .build();
        customer = customerRepository.save(customer);

        String token = jwtTokenProvider.generateToken(customer.getId(), customer.getUsername());
        return buildAuthResponse(customer, token);
    }

    public AuthResponse login(LoginRequest req) {
        CustomerEntity customer = customerRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!customer.isActive())
            throw new IllegalArgumentException("Account is deactivated");

        if (!passwordEncoder.matches(req.getPassword(), customer.getPasswordHash()) &&
            !BCrypt.checkpw(req.getPassword(), customer.getPasswordHash()))
            throw new IllegalArgumentException("Invalid username or password");

        String token = jwtTokenProvider.generateToken(customer.getId(), customer.getUsername());
        return buildAuthResponse(customer, token);
    }

    @Transactional
    public void forgotPassword(String email) {
        customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with that email"));
        // In production: generate token, send email
        // For now: return OK (email system not configured)
    }

    private AuthResponse buildAuthResponse(CustomerEntity c, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .customerId(c.getId())
                .username(c.getUsername())
                .fullName(c.getFullName())
                .loyaltyPoints(c.getLoyaltyPoints())
                .build();
    }
}
