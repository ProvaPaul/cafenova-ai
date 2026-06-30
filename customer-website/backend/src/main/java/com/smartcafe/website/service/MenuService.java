package com.smartcafe.website.service;

import com.smartcafe.website.dto.response.MenuItemResponse;
import com.smartcafe.website.entity.CategoryEntity;
import com.smartcafe.website.entity.MenuItemEntity;
import com.smartcafe.website.repository.CategoryRepository;
import com.smartcafe.website.repository.FeedbackRepository;
import com.smartcafe.website.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final FeedbackRepository feedbackRepository;

    public List<CategoryEntity> getCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    public List<MenuItemResponse> getAllItems() {
        return menuItemRepository.findByActiveTrueAndAvailableTrueOrderByNameAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MenuItemResponse> getByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryIdAndActiveTrueAndAvailableTrue(categoryId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<MenuItemResponse> search(String query) {
        return menuItemRepository.search(query)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public MenuItemResponse getById(Long id) {
        MenuItemEntity item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        return toResponse(item);
    }

    private MenuItemResponse toResponse(MenuItemEntity item) {
        Double avg = feedbackRepository.avgRatingByMenuItemId(item.getId());
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imagePath(item.getImagePath())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .available(item.isAvailable())
                .avgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                .build();
    }
}
