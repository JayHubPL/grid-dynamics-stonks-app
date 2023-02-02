package com.griddynamics.internship.stonksjh.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "finnhub")
public record FinnhubProperties(
        String apiKey
) {
}
