package xyz._3.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.streamer")
public record StreamerProperties(
        String defaultId,
        String portalKey
) {
}
