package xyz._3.social.security;

import xyz._3.social.model.UserRole;

public record AuthenticatedUser(String username, UserRole role, String streamerId) {
}
