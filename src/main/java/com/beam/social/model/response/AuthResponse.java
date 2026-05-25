package com.beam.social.model.response;

import com.beam.social.model.UserRole;

public record AuthResponse(
        String token,
        UserRole role,
        String streamerId,
        String overlayToken,
        String donationToken
) {
}
