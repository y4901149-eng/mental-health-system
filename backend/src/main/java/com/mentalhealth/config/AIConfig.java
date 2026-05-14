package com.mentalhealth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    private boolean enabled = true;
    private String provider = "deepseek";
    private String apiKey;
    private String apiUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private int maxTokens = 500;
    private double temperature = 0.7;
}
