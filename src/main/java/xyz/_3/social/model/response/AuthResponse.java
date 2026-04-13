package xyz._3.social.model.response;

import xyz._3.social.model.UserRole;

public record AuthResponse(
        String token,
        UserRole role,
        String streamerId
) {
}
