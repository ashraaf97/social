package xyz._3.social.streamer.service;

import org.springframework.stereotype.Service;
import xyz._3.social.config.StreamerProperties;

@Service
public class StreamerAuthService {
    private final StreamerProperties streamerProperties;

    public StreamerAuthService(StreamerProperties streamerProperties) {
        this.streamerProperties = streamerProperties;
    }

    public void assertAuthorized(String providedKey) {
        if (!streamerProperties.portalKey().equals(providedKey)) {
            throw new UnauthorizedStreamerAccessException();
        }
    }
}
