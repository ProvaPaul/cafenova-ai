package com.smartcafe.ai.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AiConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AiConfig.class.getResourceAsStream("/config/ai.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException ignored) {}
    }

    private AiConfig() {}

    public static String getApiBaseUrl() {
        return PROPS.getProperty("ai.api.base-url", "http://localhost:8000/api/v1");
    }

    public static int getConnectTimeoutSeconds() {
        return Integer.parseInt(PROPS.getProperty("ai.api.connect-timeout", "5"));
    }

    public static int getReadTimeoutSeconds() {
        return Integer.parseInt(PROPS.getProperty("ai.api.read-timeout", "30"));
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(PROPS.getProperty("ai.enabled", "false"));
    }
}
