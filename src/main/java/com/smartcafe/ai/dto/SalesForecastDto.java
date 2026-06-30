package com.smartcafe.ai.dto;

import java.time.LocalDate;

public record SalesForecastDto(
        LocalDate date,
        double predictedRevenue,
        double confidence
) {}
