package com.smartcafe.website.security;

import com.smartcafe.website.entity.CustomerEntity;
import com.smartcafe.website.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomerEntity customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + username));

        return new User(
                customer.getUsername(),
                customer.getPasswordHash(),
                customer.isActive(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}
