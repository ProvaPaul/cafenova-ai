package com.smartcafe.ai;

import com.smartcafe.ai.dto.CustomerInsightDto;
import com.smartcafe.ai.dto.MenuRecommendationDto;
import com.smartcafe.ai.dto.SalesForecastDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demo stub that returns plausible-looking data without calling any external service.
 *
 * Replace this with {@code AiRecommendationServiceImpl} once the Python FastAPI
 * server is running. Wire the swap in {@code AppContext.initializeStep5()}.
 */
public class AiRecommendationServiceStub implements AiRecommendationService {

    private final Random rng = new Random(42);

    @Override
    public List<MenuRecommendationDto> getMenuRecommendations() {
        return List.of(
            new MenuRecommendationDto("Caramel Macchiato",   0.92, "Peak demand on weekday mornings (7–9 AM)"),
            new MenuRecommendationDto("Matcha Latte",        0.87, "Trending among customers aged 18–25"),
            new MenuRecommendationDto("Avocado Toast",       0.84, "Popular weekend brunch upsell"),
            new MenuRecommendationDto("Iced Americano",      0.80, "Consistent bestseller in summer months"),
            new MenuRecommendationDto("Blueberry Cheesecake",0.75, "High margin, low prep time")
        );
    }

    @Override
    public List<SalesForecastDto> getSalesForecast(int daysAhead) {
        List<SalesForecastDto> result = new ArrayList<>();
        LocalDate base  = LocalDate.now();
        double    floor = 4_500.0;
        for (int i = 1; i <= daysAhead; i++) {
            double rev = floor + (rng.nextDouble() * 2_000);
            result.add(new SalesForecastDto(base.plusDays(i), rev, 0.78 + rng.nextDouble() * 0.15));
        }
        return result;
    }

    @Override
    public List<CustomerInsightDto> getCustomerInsights(int topN) {
        return List.of(
            new CustomerInsightDto("Juan Dela Cruz",  "HIGH_VALUE", "Visits every Tuesday morning; prefers hot drinks", 0.93),
            new CustomerInsightDto("Maria Santos",    "AT_RISK",    "No visit in 30+ days; send a loyalty coupon",       0.88),
            new CustomerInsightDto("Pedro Reyes",     "LOYAL",      "Consistent weekly visitor; reward with free upgrade",0.95),
            new CustomerInsightDto("Ana Gomez",       "GROWING",    "Visit frequency doubled in last 30 days",            0.82),
            new CustomerInsightDto("Carlo Villanueva","CHURNED",    "No visit in 90+ days; re-engagement campaign advised",0.76)
        );
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
