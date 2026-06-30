package com.smartcafe.website.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcafe.website.dto.response.RecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    @Value("${ai.service.url:http://localhost:8000/api/v1}")
    private String aiBaseUrl;

    private final HttpClient     httpClient;
    private final ObjectMapper   mapper;

    public RecommendationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.mapper = new ObjectMapper();
    }

    // ── Public methods called by controller ───────────────────────────────────

    public RecommendationResponse getTrending(int limit) {
        return get("/recommendations/trending?limit=" + limit);
    }

    public RecommendationResponse getPersonal(Long customerId, int limit, String context) {
        Map<String, Object> body = Map.of(
            "customer_id", customerId != null ? customerId : 0,
            "limit", limit,
            "context", context
        );
        return post("/recommendations/personal", body);
    }

    public RecommendationResponse getSimilar(int productId, String productName, String category, int limit) {
        Map<String, Object> body = Map.of(
            "product_id",   productId,
            "product_name", productName != null ? productName : "",
            "category",     category    != null ? category    : "",
            "limit",        limit
        );
        return post("/recommendations/similar", body);
    }

    public RecommendationResponse getCartRecommendations(List<Integer> productIds, List<String> productNames, int limit) {
        Map<String, Object> body = Map.of(
            "product_ids",   productIds   != null ? productIds   : List.of(),
            "product_names", productNames != null ? productNames : List.of(),
            "limit",         limit
        );
        return post("/recommendations/cart", body);
    }

    public RecommendationResponse getPosRecommendations(List<Integer> itemIds, List<String> itemNames, int limit) {
        Map<String, Object> body = Map.of(
            "current_item_ids",   itemIds   != null ? itemIds   : List.of(),
            "current_item_names", itemNames != null ? itemNames : List.of(),
            "limit",              limit
        );
        return post("/recommendations/pos", body);
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private RecommendationResponse get(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(aiBaseUrl + path))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(resp.body(), RecommendationResponse.class);
        } catch (Exception e) {
            log.warn("AI service unavailable (GET {}): {}", path, e.getMessage());
            return fallback("trending");
        }
    }

    private RecommendationResponse post(String path, Object bodyObj) {
        try {
            String json = mapper.writeValueAsString(bodyObj);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(aiBaseUrl + path))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(resp.body(), RecommendationResponse.class);
        } catch (Exception e) {
            log.warn("AI service unavailable (POST {}): {}", path, e.getMessage());
            return fallback(path.contains("pos") ? "pos" : "personal");
        }
    }

    /** Returns a static fallback when the Python AI service is offline. */
    private RecommendationResponse fallback(String context) {
        RecommendationResponse r = new RecommendationResponse();
        r.setSuccess(true);
        r.setContext(context + ":fallback");
        r.setTotal(4);

        var items = List.of(
            item(35, "Caramel Macchiato",     "Espresso Drinks",    145.00, "Trending Now",             0.95),
            item(38, "Mocha Frappuccino",     "Frappuccinos",        165.00, "Top Seller",               0.91),
            item(43, "Brown Sugar Bubble Tea","Tea & Infusions",     145.00, "Customer Favorite",        0.88),
            item(67, "Belgian Waffle Classic","Waffles & Pancakes",  165.00, "Frequently Bought Together",0.85)
        );
        r.setRecommendations(items);
        return r;
    }

    private RecommendationResponse.RecommendedItem item(
            int id, String name, String cat, double price, String reason, double conf) {
        var i = new RecommendationResponse.RecommendedItem();
        i.setId(id); i.setName(name); i.setCategory(cat);
        i.setPrice(price); i.setReason(reason); i.setConfidence(conf);
        i.setAlgorithm("fallback");
        return i;
    }
}
