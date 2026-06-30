package com.smartcafe.ai.dto;

public record CustomerInsightDto(
        String customerName,
        String segment,
        String insight,
        double score
) {}
