package com.beam.social.security;

import com.beam.social.model.UserRole;

public record AuthenticatedUser(String username, UserRole role, String streamerId) {
}
