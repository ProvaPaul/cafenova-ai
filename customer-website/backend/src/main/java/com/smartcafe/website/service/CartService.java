package com.smartcafe.website.service;

import com.smartcafe.website.entity.CartItemEntity;
import com.smartcafe.website.entity.CustomerEntity;
import com.smartcafe.website.entity.MenuItemEntity;
import com.smartcafe.website.repository.CartItemRepository;
import com.smartcafe.website.repository.CustomerRepository;
import com.smartcafe.website.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;

    public List<CartItemEntity> getCart(Long customerId) {
        return cartItemRepository.findByCustomerId(customerId);
    }

    @Transactional
    public CartItemEntity addItem(Long customerId, Long menuItemId, int quantity) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        MenuItemEntity item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        if (!item.isAvailable() || !item.isActive())
            throw new IllegalArgumentException("Item is not available");

        Optional<CartItemEntity> existing = cartItemRepository.findByCustomerIdAndMenuItemId(customerId, menuItemId);
        if (existing.isPresent()) {
            CartItemEntity ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            return cartItemRepository.save(ci);
        }

        return cartItemRepository.save(CartItemEntity.builder()
                .customer(customer).menuItem(item).quantity(quantity).build());
    }

    @Transactional
    public CartItemEntity updateQuantity(Long customerId, Long cartItemId, int quantity) {
        CartItemEntity ci = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!ci.getCustomer().getId().equals(customerId))
            throw new IllegalArgumentException("Unauthorized");
        if (quantity <= 0) {
            cartItemRepository.delete(ci);
            return null;
        }
        ci.setQuantity(quantity);
        return cartItemRepository.save(ci);
    }

    @Transactional
    public void removeItem(Long customerId, Long cartItemId) {
        CartItemEntity ci = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!ci.getCustomer().getId().equals(customerId))
            throw new IllegalArgumentException("Unauthorized");
        cartItemRepository.delete(ci);
    }

    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }
}
