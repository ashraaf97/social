package xyz._3.social.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("USERS")
public record User(
        @Id Long id,
        String username,
        String passwordHash,
        String email,
        UserRole role,
        String streamerId,
        Instant createdAt
) {
}
