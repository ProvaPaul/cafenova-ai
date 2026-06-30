package com.smartcafe.ai;

import com.smartcafe.ai.dto.CustomerInsightDto;
import com.smartcafe.ai.dto.MenuRecommendationDto;
import com.smartcafe.ai.dto.SalesForecastDto;

import java.util.List;

/**
 * Contract for the AI Recommendation Engine.
 *
 * Production implementation will delegate to a Python FastAPI server.
 * Until that server is available, {@link AiRecommendationServiceStub} is used.
 *
 * Architecture:
 *   Java Swing  →  RestApiClient  →  Python FastAPI  →  AI/ML Models
 */
public interface AiRecommendationService {

    /** Top menu items the AI predicts will sell well, with confidence scores. */
    List<MenuRecommendationDto> getMenuRecommendations();

    /** Predicted daily revenue for the next {@code daysAhead} days. */
    List<SalesForecastDto> getSalesForecast(int daysAhead);

    /** Customer segmentation and behaviour insights for the top N customers. */
    List<CustomerInsightDto> getCustomerInsights(int topN);

    /** Returns true only when the Python FastAPI server is reachable. */
    boolean isAvailable();
}
