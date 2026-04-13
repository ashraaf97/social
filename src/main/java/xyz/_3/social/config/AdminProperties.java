package xyz._3.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.admin")
public record AdminProperties(String username, String password) {
}
