package com.smartcafe.website.service;

import com.smartcafe.website.dto.request.ChangePasswordRequest;
import com.smartcafe.website.dto.request.UpdateProfileRequest;
import com.smartcafe.website.entity.CustomerEntity;
import com.smartcafe.website.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getProfile(Long customerId) {
        CustomerEntity c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return toSafeProfile(c);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long customerId, UpdateProfileRequest req) {
        CustomerEntity c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        c.setFullName(req.getFullName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        return toSafeProfile(customerRepository.save(c));
    }

    private Map<String, Object> toSafeProfile(CustomerEntity c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("fullName", c.getFullName());
        m.put("username", c.getUsername());
        m.put("email", c.getEmail());
        m.put("phone", c.getPhone());
        m.put("address", c.getAddress());
        m.put("loyaltyPoints", c.getLoyaltyPoints());
        m.put("totalSpent", c.getTotalSpent());
        m.put("visitCount", c.getVisitCount());
        m.put("loyaltyTier", tier(c.getLoyaltyPoints()));
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    private String tier(int pts) {
        if (pts >= 5000) return "Platinum";
        if (pts >= 2000) return "Gold";
        if (pts >= 500)  return "Silver";
        return "Bronze";
    }

    @Transactional
    public void changePassword(Long customerId, ChangePasswordRequest req) {
        CustomerEntity c = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (!passwordEncoder.matches(req.getCurrentPassword(), c.getPasswordHash()))
            throw new IllegalArgumentException("Current password is incorrect");
        c.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        customerRepository.save(c);
    }
}
