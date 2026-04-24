package xyz._3.social.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("ACTIVE_TOKENS")
public record ActiveToken(
        @Id String jti,
        String username,
        Instant expiresAt
) {
}
