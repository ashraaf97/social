package com.beam.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.openai")
public record OpenAiProperties(
        String apiKey,
        String voice,
        String model,
        String baseUrl
) {
}