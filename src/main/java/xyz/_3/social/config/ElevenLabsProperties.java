package xyz._3.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.elevenlabs")
public record ElevenLabsProperties(
        String apiKey,
        String voiceId,
        String modelId,
        String baseUrl
) {
}
