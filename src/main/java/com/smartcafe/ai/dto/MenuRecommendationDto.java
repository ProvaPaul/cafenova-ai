package com.smartcafe.ai.dto;

public record MenuRecommendationDto(
        String itemName,
        double confidenceScore,
        String reason
) {}
