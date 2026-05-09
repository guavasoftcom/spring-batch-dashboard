package com.guavasoft.springbatch.dashboard.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {

    /**
     * Per-registration button styling, keyed by Spring Security registration id (e.g. "github").
     * Looked up by id at request time; missing entries fall back to a capitalized id as the label
     * and null for color/iconBase64.
     */
    private Map<String, ButtonConfig> buttons = new HashMap<>();

    @Getter
    @Setter
    public static class ButtonConfig {
        private String label;
        private String color;
        private String iconUrl;
    }
}
