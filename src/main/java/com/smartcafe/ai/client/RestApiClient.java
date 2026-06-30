package com.smartcafe.ai.client;

import com.smartcafe.ai.config.AiConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin HTTP client for the Python FastAPI AI service.
 *
 * Uses the Java 11+ built-in {@link HttpClient} — no extra dependency needed.
 * All endpoints are relative to {@code ai.api.base-url} in ai.properties.
 *
 * Expected FastAPI endpoints:
 *   GET  /api/v1/health
 *   GET  /api/v1/recommendations/menu
 *   GET  /api/v1/forecast/sales?days=7
 *   GET  /api/v1/insights/customers?limit=10
 */
public class RestApiClient {

    private final HttpClient httpClient;
    private final String baseUrl;

    public RestApiClient() {
        this.baseUrl = AiConfig.getApiBaseUrl();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AiConfig.getConnectTimeoutSeconds()))
                .build();
    }

    public String get(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(AiConfig.getReadTimeoutSeconds()))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("AI API error " + response.statusCode()
                    + ": " + response.body());
        }
        return response.body();
    }

    public String post(String endpoint, String jsonBody)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(AiConfig.getReadTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("AI API error " + response.statusCode()
                    + ": " + response.body());
        }
        return response.body();
    }

    public String delete(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(AiConfig.getReadTimeoutSeconds()))
                .header("Accept", "application/json")
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("AI API error " + response.statusCode()
                    + ": " + response.body());
        }
        return response.body();
    }

    /** Quick liveness check — used by {@code isAvailable()} in the real implementation. */
    public boolean isReachable() {
        try {
            get("/health");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
