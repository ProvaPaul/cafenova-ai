package com.smartcafe.ai;

import com.smartcafe.ai.client.RestApiClient;
import com.smartcafe.ai.dto.CustomerInsightDto;
import com.smartcafe.ai.dto.MenuRecommendationDto;
import com.smartcafe.ai.dto.SalesForecastDto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Production implementation — delegates to the Python FastAPI service.
 *
 * Wire this into AppContext when the service is confirmed running:
 * {@code AppContext.initializeStep5(new AiRecommendationServiceImpl(restApiClient));}
 */
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private static final Logger LOG = Logger.getLogger(AiRecommendationServiceImpl.class.getName());

    private final RestApiClient client;
    private final AiRecommendationService fallback = new AiRecommendationServiceStub();

    public AiRecommendationServiceImpl(RestApiClient client) {
        this.client = client;
    }

    @Override
    public List<MenuRecommendationDto> getMenuRecommendations() {
        try {
            String json = client.get("/api/v1/recommendations/trending?limit=5");
            JSONObject obj  = new JSONObject(json);
            JSONArray  recs = obj.getJSONArray("recommendations");
            List<MenuRecommendationDto> result = new ArrayList<>();
            for (int i = 0; i < recs.length(); i++) {
                JSONObject r = recs.getJSONObject(i);
                result.add(new MenuRecommendationDto(
                    r.optString("recommendation", r.optString("name", "Unknown")),
                    r.getDouble("confidence"),
                    r.getString("reason")
                ));
            }
            return result;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "FastAPI unavailable — using stub recommendations: {0}", e.getMessage());
            return fallback.getMenuRecommendations();
        }
    }

    @Override
    public List<SalesForecastDto> getSalesForecast(int daysAhead) {
        // Analytics endpoints are admin-only stubs for now — use stub data
        return fallback.getSalesForecast(daysAhead);
    }

    @Override
    public List<CustomerInsightDto> getCustomerInsights(int topN) {
        return fallback.getCustomerInsights(topN);
    }

    @Override
    public boolean isAvailable() {
        return client.isReachable();
    }
}
