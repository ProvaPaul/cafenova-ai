package com.smartcafe.website.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imagePath;
    private Long categoryId;
    private String categoryName;
    private boolean available;
    private Double avgRating;
    private int reviewCount;
}
