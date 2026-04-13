package xyz._3.social.model.response;

import java.time.Instant;

public record StreamerProfileResponse(
        Long id,
        String username,
        String email,
        String streamerId,
        Instant createdAt
) {
}
