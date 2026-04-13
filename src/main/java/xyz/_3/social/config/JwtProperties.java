package xyz._3.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
